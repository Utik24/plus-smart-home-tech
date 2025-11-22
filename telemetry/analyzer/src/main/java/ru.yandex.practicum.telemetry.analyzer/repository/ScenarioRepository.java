package ru.yandex.practicum.telemetry.analyzer.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.telemetry.analyzer.model.Scenario;

import java.util.List;
import java.util.Optional;

public interface ScenarioRepository extends JpaRepository<Scenario, Long> {
    List<Scenario> findByHubId(String hubId);

    @EntityGraph(attributePaths = {
            "conditions.sensor",
            "conditions.condition",
            "actions.sensor",
            "actions.action"
    })
    List<Scenario> findWithSensorsByHubId(String hubId);
    Optional<Scenario> findByHubIdAndName(String hubId, String name);
}