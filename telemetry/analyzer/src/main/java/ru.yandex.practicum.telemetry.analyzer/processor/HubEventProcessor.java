package ru.yandex.practicum.telemetry.analyzer.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.analyzer.config.AnalyzerKafkaProperties;
import ru.yandex.practicum.telemetry.analyzer.service.AnalyzerService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class HubEventProcessor implements Runnable {
    private final AnalyzerService analyzerService;
    private final AnalyzerKafkaProperties.ConsumerConfig consumerConfig;
    private final KafkaConsumer<String, HubEventAvro> consumer;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new ConcurrentHashMap<>();

    @Autowired
    public HubEventProcessor(AnalyzerService analyzerService, AnalyzerKafkaProperties properties) {
        this.analyzerService = analyzerService;
        this.consumerConfig = properties.getHubConsumer();
        this.consumer = new KafkaConsumer<>(consumerConfig.getProperties());

        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
    }

    @Override
    public void run() {
        try {
            consumer.subscribe(List.of(consumerConfig.getTopic()));
            log.info("Запущен обработчик событий хаба для топика {}", consumerConfig.getTopic());

            while (true) {
                ConsumerRecords<String, HubEventAvro> records = consumer.poll(consumerConfig.getPollTimeout());
                if (records.isEmpty()) {
                    continue;
                }

                for (ConsumerRecord<String, HubEventAvro> record : records) {
                    try {
                        analyzerService.handleHubEvent(record.value());
                        currentOffsets.put(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset() + 1));
                    } catch (Exception e) {
                        log.error("Ошибка при обработке события хаба с оффсетом {}", record.offset(), e);
                    }
                }

                consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                    if (exception != null) {
                        log.warn("Ошибка при фиксации оффсетов событий хаба: {}", offsets, exception);
                    }
                });
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("Ошибка при обработке событий хаба", e);
        } finally {
            try {
                consumer.commitSync(currentOffsets);
            } finally {
                consumer.close();
            }
        }
    }
}