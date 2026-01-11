package ru.yandex.practicum.commerce.shoppingcart.entity.dto;

import lombok.*;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCartDto {
    private UUID shoppingCartId;
    private Map<UUID, Long> products;
}
