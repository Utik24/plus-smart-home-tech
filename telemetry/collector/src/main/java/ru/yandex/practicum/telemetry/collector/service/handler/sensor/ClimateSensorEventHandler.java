package ru.yandex.practicum.telemetry.collector.service.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.telemetry.collector.model.sensor.ClimateSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.enums.SensorEventType;
import ru.yandex.practicum.telemetry.collector.service.KafkaEventProducer;

@Component
public class ClimateSensorEventHandler extends BaseSensorEventHandler<ClimateSensorAvro> {
    public ClimateSensorEventHandler(KafkaEventProducer producer) {
        super(producer);
    }

    @Override
    protected ClimateSensorAvro mapToAvro(SensorEvent event) {
        ClimateSensorEvent _event = (ClimateSensorEvent) event;

        return ClimateSensorAvro.newBuilder()
                .setTemperatureC(_event.getTemperatureC())
                .setHumidity(_event.getHumidity())
                .setCo2Level(_event.getCo2Level())
                .build();
    }

    @Override
    public SensorEventType getMessageType() {
        return SensorEventType.CLIMATE_SENSOR_EVENT;
    }
}
