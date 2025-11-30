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
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.analyzer.config.AnalyzerKafkaProperties;
import ru.yandex.practicum.telemetry.analyzer.service.AnalyzerService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SnapshotProcessor {
    private final AnalyzerService analyzerService;
    private final AnalyzerKafkaProperties.ConsumerConfig consumerConfig;
    private final KafkaConsumer<String, SensorsSnapshotAvro> consumer;

    @Autowired
    public SnapshotProcessor(AnalyzerService analyzerService, AnalyzerKafkaProperties properties) {
        this.analyzerService = analyzerService;
        this.consumerConfig = properties.getSnapshotConsumer();
        this.consumer = new KafkaConsumer<>(consumerConfig.getProperties());

        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
    }

    public void start() {
        try {
            consumer.subscribe(List.of(consumerConfig.getTopic()));
            log.info("Запущен обработчик снапшотов для топика {}", consumerConfig.getTopic());

            while (true) {
                ConsumerRecords<String, SensorsSnapshotAvro> consumerRecords = consumer.poll(consumerConfig.getPollTimeout());
                if (consumerRecords.isEmpty()) {
                    continue;
                }

                Map<TopicPartition, OffsetAndMetadata> offsetsToCommit = new HashMap<>();

                for (TopicPartition partition : consumerRecords.partitions()) {
                    List<ConsumerRecord<String, SensorsSnapshotAvro>> partitionRecords = consumerRecords.records(partition);

                    for (ConsumerRecord<String, SensorsSnapshotAvro> record : partitionRecords) {
                        analyzerService.processSnapshot(record.value());
                    }

                    long nextOffset = partitionRecords.get(partitionRecords.size() - 1).offset() + 1;
                    offsetsToCommit.put(partition, new OffsetAndMetadata(nextOffset));
                }

                consumer.commitAsync(offsetsToCommit, (offsets, exception) -> {
                    if (exception != null) {
                        log.warn("Ошибка при фиксации оффсетов снапшотов: {}", offsets, exception);
                    }
                });
            }
        } catch (WakeupException ignored) {
            // ignore, shutdown triggered
        } catch (Exception e) {
            log.error("Ошибка при обработке снапшотов", e);
        } finally {
            try {
                consumer.commitSync();
            } finally {
                consumer.close();
            }
        }
    }
}