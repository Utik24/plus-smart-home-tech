package ru.yandex.practicum.telemetry.analyzer.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Properties;

@Getter
@AllArgsConstructor
@ConfigurationProperties("analyzer.kafka")
public class AnalyzerKafkaProperties {
    private final ConsumerConfig snapshotConsumer;
    private final ConsumerConfig hubConsumer;

    @Setter
    @Getter
    @AllArgsConstructor
    public static class ConsumerConfig {
        private String topic;
        private Duration pollTimeout;
        private Properties properties;
    }
}