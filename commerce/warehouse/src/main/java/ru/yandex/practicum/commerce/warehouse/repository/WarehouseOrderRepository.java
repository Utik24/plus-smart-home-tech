package ru.yandex.practicum.commerce.warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.commerce.warehouse.entity.OrderBooking;

import java.util.UUID;

@Repository
public interface WarehouseOrderRepository extends JpaRepository<OrderBooking, UUID>{
}
