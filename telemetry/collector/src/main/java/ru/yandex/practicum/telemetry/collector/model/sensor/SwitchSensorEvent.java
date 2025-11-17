package ru.yandex.practicum.telemetry.collector.model.sensor;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.telemetry.collector.model.sensor.enums.SensorEventType;

@Getter
@Setter
@ToString(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SwitchSensorEvent extends SensorEvent {
    Boolean state;

    @Override
    public SensorEventType getType() {
        return SensorEventType.SWITCH_SENSOR_EVENT;
    }
}
