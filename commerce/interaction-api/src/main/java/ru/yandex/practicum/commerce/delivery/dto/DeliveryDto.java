package ru.yandex.practicum.commerce.delivery.dto;

import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.commerce.delivery.enums.DeliveryState;
import ru.yandex.practicum.commerce.warehouse.entity.dto.AddressDto;

import java.util.UUID;

@Getter
@Setter
public class DeliveryDto {
    private UUID deliveryId;
    private AddressDto fromAddress;
    private AddressDto toAddress;
    private UUID orderId;
    private DeliveryState deliveryState;
}
