package ru.yandex.practicum.commerce.shoppingcart.controller;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.shoppingcart.entity.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.shoppingcart.entity.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.shoppingcart.service.CartService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/shopping-cart")
@RequiredArgsConstructor
public class ShoppingCartController implements ShoppingCartClient {
    private final CartService cartService;

    @Override
    public ShoppingCartDto getShoppingCart(@RequestParam(name = "username") String username) throws FeignException {
        log.info("Получить актуальную корзину для авторизованного пользователя {}.", username);
        return cartService.getShoppingCart(username);
    }

    @Override
    public ShoppingCartDto addToCart(Map<UUID, Integer> products,
                                     @RequestParam(name = "username") String username) throws FeignException {
        log.info("{} добавил товар {} в корзину.", username, products);
        return cartService.addToCartProduct(username, products);
    }

    @Override
    public void deleteCart(@RequestParam(name = "username") String username) throws FeignException {
        log.info("Деактивация корзины товаров для пользователя {}.", username);
        cartService.deleteCart(username);
    }

    @Override
    public ShoppingCartDto removeFromCart(List<UUID> products, @RequestParam(name = "username") String username) throws FeignException {
        return cartService.removeFromCart(products, username);
    }

    @Override
    public ShoppingCartDto changeProductQuantity(ChangeProductQuantityRequest request, @RequestParam(name = "username") String username) throws FeignException {
        log.info("Изменить количество товаров в корзине.");
        return cartService.changeProductQuantity(request, username);
    }
}
