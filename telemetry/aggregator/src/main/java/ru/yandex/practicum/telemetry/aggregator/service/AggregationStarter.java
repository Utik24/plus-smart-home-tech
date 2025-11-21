package ru.yandex.practicum.telemetry.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.aggregator.config.KafkaConfig;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {
    private final SnapshotService snapshotService;

    // Хранилище обработанных офсетов должно быть потокобезопасным
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new ConcurrentHashMap<>();

    private final KafkaConsumer<String, SensorEventAvro> consumer;
    private final KafkaConfig.ConsumerConfig consumerConfig;

    private final KafkaProducer<String, SensorsSnapshotAvro> producer;
    private final KafkaConfig.ProducerConfig producerConfig;

    @Autowired
    public AggregationStarter(SnapshotService snapshotService, KafkaConfig kafkaConfig) {
        this.snapshotService = snapshotService;
        this.consumerConfig = kafkaConfig.getConsumer();
        this.producerConfig = kafkaConfig.getProducer();

        this.consumer = new KafkaConsumer<>(consumerConfig.getProperties());
        this.producer = new KafkaProducer<>(producerConfig.getProperties());

        // Регистрируем хук для корректного завершения работы
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            consumer.wakeup();
        }));
    }

    /**
     * Метод для начала процесса агрегации данных.
     * Подписывается на топики для получения событий от датчиков,
     * формирует снимок их состояния и записывает в кафку.
     */
    public void start() {
        try {
            log.trace("Подписываюсь на топик \"{}\" для получения событий от датчиков", consumerConfig.getTopic());
            consumer.subscribe(List.of(consumerConfig.getTopic()));

            // Цикл обработки событий
            while (true) {
                ConsumerRecords<String, SensorEventAvro> consumerRecords = consumer.poll(consumerConfig.getPollTimeout());

                if (!consumerRecords.isEmpty()) {
                    for (ConsumerRecord<String, SensorEventAvro> record : consumerRecords) {
                        // Обработка
                        handleEvent(record.value());

                        // Обновляем офсет для фиксации
                        currentOffsets.put(
                                new TopicPartition(record.topic(), record.partition()),
                                new OffsetAndMetadata(record.offset() + 1)
                        );
                    }

                    producer.flush(); // Отправляем все собранные снапшоты

                    // Асинхронно фиксируем офсеты пакета
                    consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                        if (exception != null) {
                            log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                        }
                    });
                }
            }

        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер и продюсер в блоке finally
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {

            try {
                // Перед тем, как закрыть продюсер и консьюмер, нужно убедится,
                // что все сообщения, лежащие в буффере, отправлены и
                // все оффсеты обработанных сообщений зафиксированы

                if (producer != null) {
                    producer.flush();
                }
                if (consumer != null) {
                    // ИСПРАВЛЕНИЕ: Синхронная фиксация обработанных офсетов
                    consumer.commitSync(currentOffsets);
                }

            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }

    private void handleEvent(SensorEventAvro event) {
        // Если полученное событие от датчика содержит новые значения, то
        // обновляем состояние датчиков хаба данными от полученного события
        Optional<SensorsSnapshotAvro> updatedState = snapshotService.updateState(event);

        // Если состояние было обновлено, отправляем его в топик снэпшотов
        if (updatedState.isPresent()) {
            SensorsSnapshotAvro sensorsSnapshot = updatedState.get();

            log.info("Событие датчика {} обновило состояние снапшота. " +
                            "Сохраняю снапшот состояния датчиков хаба {} от {} в топик {}",
                    event.getId(), sensorsSnapshot.getHubId().toString(), sensorsSnapshot.getTimestamp(),
                    producerConfig.getTopic());

            ProducerRecord<String, SensorsSnapshotAvro> recordToSend =
                    new ProducerRecord<>(
                            producerConfig.getTopic(),
                            null,
                            sensorsSnapshot.getTimestamp().toEpochMilli(),
                            sensorsSnapshot.getHubId().toString(),
                            sensorsSnapshot
                    );
            producer.send(recordToSend, (metadata, exception) -> {
                if (exception == null) {
                    log.info("Снапшот был сохранён в партицию {} со смещением {}", metadata.partition(), metadata.offset());
                } else {
                    log.warn("Не удалось записать снапшот в топик {}", producerConfig.getTopic(), exception);
                }
            });
        } else {
            log.trace("Событие от датчика {} хаба {} не обновило состояние снапшота", event.getId(), event.getHubId());
        }
    }
}