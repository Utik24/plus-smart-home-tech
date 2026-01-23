package ru.yandex.practicum.commerce.warehouse.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class ShippedToDeleveryRequest {
    UUID orderId;
    UUID deliveryId;
}
