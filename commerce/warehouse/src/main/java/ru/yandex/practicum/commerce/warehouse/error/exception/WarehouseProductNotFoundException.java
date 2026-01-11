package ru.yandex.practicum.commerce.warehouse.error.exception;

import java.util.UUID;

public class WarehouseProductNotFoundException extends RuntimeException {
    public WarehouseProductNotFoundException(UUID productId) {
        super("Товар с id " + productId + " не найден на складе");
    }
}