package ru.yandex.practicum.commerce.shoppingcart.entity.dto;

import lombok.*;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ShoppingCartDto {
    private final UUID shoppingCartId;
    private final Map<UUID, Long> products;
}
