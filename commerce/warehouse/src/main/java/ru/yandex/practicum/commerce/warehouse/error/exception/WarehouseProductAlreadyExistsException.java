package ru.yandex.practicum.commerce.warehouse.error.exception;

import java.util.UUID;

public class WarehouseProductAlreadyExistsException extends RuntimeException {
    public WarehouseProductAlreadyExistsException(UUID productId) {
        super("Товар с id " + productId + " уже добавлен на склад");
    }
}