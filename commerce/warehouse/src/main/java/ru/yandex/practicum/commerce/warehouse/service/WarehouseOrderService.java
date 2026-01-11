package ru.yandex.practicum.commerce.warehouse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.commerce.warehouse.entity.OrderBooking;
import ru.yandex.practicum.commerce.warehouse.repository.WarehouseOrderRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseOrderService {
    private final WarehouseOrderRepository warehouseOrderRepository;

    public void save(OrderBooking orderBooking) {
        warehouseOrderRepository.save(orderBooking);
    }
}
