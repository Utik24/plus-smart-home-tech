package ru.yandex.practicum.commerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.commerce.shoppingcart.entity.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.warehouse.entity.dto.AddressDto;


@Getter
@Setter
@Builder
@AllArgsConstructor
public class CreateNewOrderRequest {
    private ShoppingCartDto shoppingCart;
    private AddressDto deliveryAddress;
}
