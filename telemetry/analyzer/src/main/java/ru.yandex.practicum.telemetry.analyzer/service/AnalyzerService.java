package ru.yandex.practicum.telemetry.analyzer.service;

import com.google.protobuf.util.Timestamps;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.analyzer.model.*;
import ru.yandex.practicum.telemetry.analyzer.model.enums.ActionType;
import ru.yandex.practicum.telemetry.analyzer.model.enums.ConditionOperation;
import ru.yandex.practicum.telemetry.analyzer.model.enums.ConditionType;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.SensorRepository;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyzerService {
    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;

    @GrpcClient("hub-router")
    private HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;

    @Transactional
    public void handleHubEvent(HubEventAvro hubEventAvro) {
        String hubId = hubEventAvro.getHubId().toString();
        Object payload = hubEventAvro.getPayload();

        if (payload instanceof DeviceAddedEventAvro deviceAddedEventAvro) {
            registerDevice(hubId, deviceAddedEventAvro);
        } else if (payload instanceof DeviceRemovedEventAvro deviceRemovedEventAvro) {
            removeDevice(hubId, deviceRemovedEventAvro);
        } else if (payload instanceof ScenarioAddedEventAvro scenarioAddedEventAvro) {
            saveScenario(hubId, scenarioAddedEventAvro);
        } else if (payload instanceof ScenarioRemovedEventAvro scenarioRemovedEventAvro) {
            removeScenario(hubId, scenarioRemovedEventAvro);
        }
    }

    @Transactional
    public void processSnapshot(SensorsSnapshotAvro snapshotAvro) {
        String hubId = snapshotAvro.getHubId().toString();
        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);

        if (scenarios.isEmpty()) {
            log.trace("Для хаба {} нет сценариев для проверки", hubId);
            return;
        }

        for (Scenario scenario : scenarios) {
            if (isScenarioSatisfied(scenario, snapshotAvro)) {
                sendActions(scenario, snapshotAvro);
            }
        }
    }

    private void registerDevice(String hubId, DeviceAddedEventAvro deviceAddedEventAvro) {
        String sensorId = deviceAddedEventAvro.getId().toString();
        sensorRepository.findByIdAndHubId(sensorId, hubId)
                .or(() -> Optional.of(sensorRepository.save(Sensor.builder().id(sensorId).hubId(hubId).build())));
        log.info("Зарегистрировано устройство {} для хаба {}", sensorId, hubId);
    }

    private void removeDevice(String hubId, DeviceRemovedEventAvro deviceRemovedEventAvro) {
        String sensorId = deviceRemovedEventAvro.getId().toString();
        sensorRepository.findByIdAndHubId(sensorId, hubId).ifPresent(sensor -> {
            List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);
            for (Scenario scenario : scenarios) {
                boolean changed = scenario.getConditions().removeIf(link -> sensorId.equals(link.getSensor().getId()));
                changed |= scenario.getActions().removeIf(link -> sensorId.equals(link.getSensor().getId()));
                if (changed) {
                    scenarioRepository.save(scenario);
                }
            }
            sensorRepository.delete(sensor);
            log.info("Удалено устройство {} из хаба {}", sensorId, hubId);
        });
    }

    private void saveScenario(String hubId, ScenarioAddedEventAvro scenarioAddedEventAvro) {
        String name = scenarioAddedEventAvro.getName().toString();
        Scenario scenario = scenarioRepository.findByHubIdAndName(hubId, name)
                .orElseGet(() -> Scenario.builder().hubId(hubId).name(name).build());

        scenario.setHubId(hubId);
        scenario.setName(name);
        scenario.getConditions().clear();
        scenario.getActions().clear();

        Map<String, Sensor> sensors = new HashMap<>();

        scenarioAddedEventAvro.getConditions().forEach(conditionAvro -> {
            String sensorId = conditionAvro.getSensorId().toString();
            Sensor sensor = sensors.computeIfAbsent(sensorId, id -> ensureSensorExists(id, hubId));
            Condition condition = Condition.builder()
                    .type(ConditionType.valueOf(conditionAvro.getType().name()))
                    .operation(ConditionOperation.valueOf(conditionAvro.getOperation().name()))
                    .value(asInteger(conditionAvro.getValue()))
                    .build();

            ScenarioCondition scenarioCondition = ScenarioCondition.builder()
                    .id(new ScenarioConditionId())
                    .scenario(scenario)
                    .sensor(sensor)
                    .condition(condition)
                    .build();
            scenario.getConditions().add(scenarioCondition);
        });

        scenarioAddedEventAvro.getActions().forEach(actionAvro -> {
            String sensorId = actionAvro.getSensorId().toString();
            Sensor sensor = sensors.computeIfAbsent(sensorId, id -> ensureSensorExists(id, hubId));
            Action action = Action.builder()
                    .type(ActionType.valueOf(actionAvro.getType().name()))
                    .value(asInteger(actionAvro.getValue()))
                    .build();

            ScenarioAction scenarioAction = ScenarioAction.builder()
                    .id(new ScenarioActionId())
                    .scenario(scenario)
                    .sensor(sensor)
                    .action(action)
                    .build();
            scenario.getActions().add(scenarioAction);
        });

        scenarioRepository.save(scenario);
        log.info("Сценарий {} для хаба {} сохранён", name, hubId);
    }

    private Sensor ensureSensorExists(String sensorId, String hubId) {
        return sensorRepository.findByIdAndHubId(sensorId, hubId)
                .orElseGet(() -> sensorRepository.save(Sensor.builder().id(sensorId).hubId(hubId).build()));
    }

    private void removeScenario(String hubId, ScenarioRemovedEventAvro scenarioRemovedEventAvro) {
        scenarioRepository.findByHubIdAndName(hubId, scenarioRemovedEventAvro.getName().toString()).ifPresent(scenario -> {
            scenarioRepository.delete(scenario);
            log.info("Сценарий {} для хаба {} удалён", scenarioRemovedEventAvro.getName(), hubId);
        });
    }

    private boolean isScenarioSatisfied(Scenario scenario, SensorsSnapshotAvro snapshotAvro) {
        Map<String, SensorStateAvro> sensorsState = snapshotAvro.getSensorsState();

        return scenario.getConditions().stream().allMatch(link -> {
            SensorStateAvro sensorStateAvro = sensorsState.get(link.getSensor().getId());
            if (sensorStateAvro == null) {
                return false;
            }

            Optional<Integer> currentValue = readValue(link.getCondition().getType(), sensorStateAvro.getPayload());
            return currentValue.filter(value -> compareValues(value, link.getCondition())).isPresent();
        });
    }

    private Optional<Integer> readValue(ConditionType type, Object payload) {
        return switch (type) {
            case MOTION -> payload instanceof MotionSensorAvro m ? Optional.of(m.getMotion() ? 1 : 0) : Optional.empty();
            case LUMINOSITY -> payload instanceof LightSensorAvro light ? Optional.of(light.getLuminosity()) : Optional.empty();
            case SWITCH -> payload instanceof SwitchSensorAvro sensor ? Optional.of(sensor.getState() ? 1 : 0) : Optional.empty();
            case TEMPERATURE -> {
                if (payload instanceof TemperatureSensorAvro temp) {
                    yield Optional.of(temp.getTemperatureC());
                } else if (payload instanceof ClimateSensorAvro climate) {
                    yield Optional.of(climate.getTemperatureC());
                }
                yield Optional.empty();
            }
            case CO2LEVEL -> payload instanceof ClimateSensorAvro climate ? Optional.of(climate.getCo2Level()) : Optional.empty();
            case HUMIDITY -> payload instanceof ClimateSensorAvro climate ? Optional.of(climate.getHumidity()) : Optional.empty();
        };
    }

    private boolean compareValues(Integer currentValue, Condition condition) {
        Integer expected = condition.getValue();
        if (expected == null) {
            return false;
        }

        return switch (condition.getOperation()) {
            case EQUALS -> Objects.equals(currentValue, expected);
            case GREATER_THAN -> currentValue > expected;
            case LOWER_THAN -> currentValue < expected;
        };
    }

    private void sendActions(Scenario scenario, SensorsSnapshotAvro snapshotAvro) {
        for (ScenarioAction action : scenario.getActions()) {
            DeviceActionProto.Builder actionBuilder = DeviceActionProto.newBuilder()
                    .setSensorId(action.getSensor().getId())
                    .setType(DeviceActionProto.ActionTypeProto.valueOf(action.getAction().getType().name()));
            if (action.getAction().getValue() != null) {
                actionBuilder.setValue(action.getAction().getValue());
            }

            DeviceActionRequest request = DeviceActionRequest.newBuilder()
                    .setHubId(snapshotAvro.getHubId().toString())
                    .setScenarioName(scenario.getName())
                    .setAction(actionBuilder.build())
                    .setTimestamp(Timestamps.fromMillis(snapshotAvro.getTimestamp().toEpochMilli()))
                    .build();

            hubRouterClient.handleDeviceAction(request);
            log.info("Отправлено действие {} для сценария {} хаба {}", action.getAction().getType(), scenario.getName(), scenario.getHubId());
        }
    }

    private Integer asInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer integer) {
            return integer;
        }
        if (value instanceof Boolean bool) {
            return bool ? 1 : 0;
        }
        return null;
    }
}