package ru.yandex.practicum.commerce.shoppingcart.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.yandex.practicum.commerce.shoppingcart.entity.ShoppingCart;
import ru.yandex.practicum.commerce.shoppingcart.entity.dto.ShoppingCartDto;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface CartMapper {

    @Mapping(target = "username", ignore = true)
    @Mapping(target = "active", ignore = true)
    ShoppingCart toShoppingCart(final ShoppingCartDto productDto);

    ShoppingCartDto toShoppingCartDto(final ShoppingCart product);

}