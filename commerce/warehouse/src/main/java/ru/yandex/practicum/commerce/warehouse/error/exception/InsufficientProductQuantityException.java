package ru.yandex.practicum.commerce.warehouse.error.exception;

import java.util.UUID;

public class InsufficientProductQuantityException extends RuntimeException {
    public InsufficientProductQuantityException(UUID productId, long requested, long available) {
        super("Недостаточно товара " + productId + " на складе. Запрошено: " + requested + ", доступно: " + available);
    }
}