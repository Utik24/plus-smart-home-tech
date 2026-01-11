package ru.yandex.practicum.commerce.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.commerce.order.enums.OrderState;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class OrderDto {
    private UUID orderId;

    @NotBlank
    private UUID shoppingCartId;

    private Map<UUID, Long> products;

    private UUID paymentId;

    private UUID deliveryId;

    private OrderState state;

    private Double deliveryWeight;

    private Double deliveryVolume;

    private Boolean fragile;

    private Double totalPrice;

    private Double deliveryPrice;

    private Double productPrice;


}
