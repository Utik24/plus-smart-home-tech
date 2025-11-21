package ru.yandex.practicum.telemetry.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotServiceImpl implements SnapshotService {
    // Хранилище должно быть потокобезопасным
    private final Map<String, SensorsSnapshotAvro> snapshots = new ConcurrentHashMap<>();

    @Override
    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro eventAvro) {
        String hubId = eventAvro.getHubId().toString();
        String sensorId = eventAvro.getId().toString();

        // 1. Получение или создание снапшота
        final SensorsSnapshotAvro snapshotAvro = snapshots.computeIfAbsent(
                hubId,
                k -> SensorsSnapshotAvro.newBuilder()
                        .setHubId(hubId)
                        .setTimestamp(eventAvro.getTimestamp())
                        // Внутреннее состояние также должно быть потокобезопасным
                        .setSensorsState(new java.util.concurrent.ConcurrentHashMap<>())
                        .build()
        );

        Map<String, SensorStateAvro> sensorStateAvros = snapshotAvro.getSensorsState();
        SensorStateAvro oldSensorStateAvro = sensorStateAvros.get(sensorId);

        long eventTimestamp = eventAvro.getTimestamp().toEpochMilli();

        if (oldSensorStateAvro != null) {
            long oldTimestamp = oldSensorStateAvro.getTimestamp().toEpochMilli();

            // 2. Проверка по времени: игнорируем только события с более старым таймстемпом
            if (oldTimestamp > eventTimestamp) {
                log.trace("Событие от датчика {} игнорировано: старый таймстемп {} новее нового {}",
                        sensorId, Instant.ofEpochMilli(oldTimestamp), Instant.ofEpochMilli(eventTimestamp));
                return Optional.empty();
            }

            // 3. Проверка по данным: игнорируем дубли с теми же данными
            if (oldTimestamp == eventTimestamp && oldSensorStateAvro.getPayload().equals(eventAvro.getPayload())) {
                log.trace("Событие от датчика {} игнорировано: данные не изменились", sensorId);
                return Optional.empty();
            }
        }

        // 4. Обновление снапшота
        SensorStateAvro newSensorStateAvro = SensorStateAvro.newBuilder()
                .setTimestamp(eventAvro.getTimestamp())
                .setPayload(eventAvro.getPayload())
                .build();

        sensorStateAvros.put(sensorId, newSensorStateAvro);

        // Обновляем метку времени самого снапшота на время последнего полученного события
        snapshotAvro.setTimestamp(newSensorStateAvro.getTimestamp());

        log.info("Снапшот хаба {} обновлен событием от датчика {}", hubId, sensorId);

        // 5. Возвращаем обновленный снапшот
        SensorsSnapshotAvro snapshotCopy = SensorsSnapshotAvro.newBuilder()
                .setHubId(snapshotAvro.getHubId())
                .setTimestamp(snapshotAvro.getTimestamp())
                .setSensorsState(new java.util.HashMap<>(snapshotAvro.getSensorsState()))
                .build();

        return Optional.of(snapshotCopy);
    }
}