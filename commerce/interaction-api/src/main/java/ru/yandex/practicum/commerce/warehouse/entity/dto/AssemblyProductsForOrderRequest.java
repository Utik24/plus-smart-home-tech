package ru.yandex.practicum.commerce.warehouse.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class AssemblyProductsForOrderRequest {
    UUID orderId;
    Map<UUID, Long> products;
}
