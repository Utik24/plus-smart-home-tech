package ru.yandex.practicum.commerce.warehouse.entity.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class NewProductInWarehouseRequest {
    @NotNull(message = "productId не может быть пустым.")
    private UUID productId;

    @NotNull(message = "fragile не может быть пустым.")
    private boolean fragile;

    @NotNull(message = "dimension не может быть пустым.")
    private DimensionDto dimension;

    @DecimalMin(value = "1.0", message = "weight должен быть минимум 1")
    private double weight;
}
