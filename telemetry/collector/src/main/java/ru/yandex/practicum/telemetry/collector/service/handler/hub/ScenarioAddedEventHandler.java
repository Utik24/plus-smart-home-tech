package ru.yandex.practicum.telemetry.collector.service.handler.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.collector.model.hub.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.ScenarioAddedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.enums.HubEventType;
import ru.yandex.practicum.telemetry.collector.service.KafkaEventProducer;

import java.util.stream.Collectors;

@Component
public class ScenarioAddedEventHandler extends BaseHubEventHandler<ScenarioAddedEventAvro> {
    public ScenarioAddedEventHandler(KafkaEventProducer producer) {
        super(producer);
    }

    @Override
    public HubEventType getMessageType() {
        return HubEventType.SCENARIO_ADDED;
    }

    @Override
    protected ScenarioAddedEventAvro mapToAvro(HubEvent event) {
        ScenarioAddedEvent _event = (ScenarioAddedEvent) event;

        return ScenarioAddedEventAvro.newBuilder()
                .setName(_event.getName())
                .setConditions(_event.getConditions().stream()
                        .map(condition -> ScenarioConditionAvro.newBuilder()
                                .setSensorId(condition.getSensorId())
                                .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                                .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()))
                                .setValue(condition.getValue())
                                .build())
                        .collect(Collectors.toList()))
                .setActions(_event.getActions().stream()
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