package ru.yandex.practicum.telemetry.collector.model.hub;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.telemetry.collector.model.hub.enums.ConditionOperation;
import ru.yandex.practicum.telemetry.collector.model.hub.enums.ConditionType;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScenarioCondition {
    @NotBlank
    String sensorId;
    @NotNull
    ConditionType type;
    @NotNull
    ConditionOperation operation;
    @NotNull
    Integer value;
}
