package ru.yandex.practicum.commerce.warehouse.entity.dto;

import jakarta.validation.constraints.Min;
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
public class AddProductToWarehouseRequest {
    @NotNull(message = "productId не может быть пустым.")
    private UUID productId;

    @Min(value = 1, message = "quantity должен быть минимум 1")
    private Long quantity;
}
