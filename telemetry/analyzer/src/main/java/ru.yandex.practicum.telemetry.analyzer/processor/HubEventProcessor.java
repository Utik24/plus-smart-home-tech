package ru.yandex.practicum.telemetry.analyzer.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.analyzer.config.AnalyzerKafkaProperties;
import ru.yandex.practicum.telemetry.analyzer.service.AnalyzerService;

import java.util.List;

@Slf4j
@Component
public class HubEventProcessor implements Runnable {
    private final AnalyzerService analyzerService;
    private final AnalyzerKafkaProperties.ConsumerConfig consumerConfig;
    private final KafkaConsumer<String, HubEventAvro> consumer;

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
                for (ConsumerRecord<String, HubEventAvro> record : records) {
                    analyzerService.handleHubEvent(record.value());
                }
            }
        } catch (WakeupException ignored) {
            // shutdown hook
        } catch (Exception e) {
            log.error("Ошибка при обработке событий хаба", e);
        } finally {
            consumer.close();
        }
    }
}