package ru.yandex.practicum.telemetry.collector.service.handler.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.collector.service.KafkaEventProducer;

import java.util.stream.Collectors;

@Component
public class ScenarioAddedEventHandler extends BaseHubEventHandler<ScenarioAddedEventAvro> {
    public ScenarioAddedEventHandler(KafkaEventProducer producer) {
        super(producer);
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }

    @Override
    protected ScenarioAddedEventAvro mapToAvro(HubEventProto event) {
        ScenarioAddedEventProto _event = event.getScenarioAdded();

        return ScenarioAddedEventAvro.newBuilder()
                .setName(_event.getName())
                .setConditions(_event.getConditionList().stream()
                        .map(condition -> ScenarioConditionAvro.newBuilder()
                                .setSensorId(condition.getSensorId())
                                .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                                .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()))
                                .setValue(condition.getValueCase().equals(ScenarioConditionProto.ValueCase.BOOL_VALUE) ? condition.getBoolValue() : condition.getIntValue())
                                .build())
                        .collect(Collectors.toList()))
                .setActions(_event.getActionList().stream()
                        .map(action -> DeviceActionAvro.newBuilder()
                                .setSensorId(action.getSensorId())
                                .setType(ActionTypeAvro.valueOf(action.getType().name()))
                                .setValue(action.getValue())
                                .build())
                        .collect(Collectors.toList())
                )
                .build();
    }
}