package ru.yandex.practicum.commerce.shoppingcart.controller;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
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
    public ShoppingCartDto getShoppingCart(@RequestParam String username) throws FeignException {
        log.info("Получить актуальную корзину для авторизованного пользователя {}.", username);
        return cartService.getShoppingCart(username);
    }

    @Override
    public ShoppingCartDto addToCart(@RequestBody Map<UUID, Integer> products,
                                     @RequestParam(name = "username") String username) throws FeignException {
        log.info("{} добавил товар {} в корзину.", username, products);
        return cartService.addToCartProduct(username, products);
    }

    @Override
    public void deleteCart(@RequestParam String username) throws FeignException {
        log.info("Деактивация корзины товаров для пользователя {}.", username);
        cartService.deleteCart(username);
    }

    @Override
    public ShoppingCartDto removeFromCart(@RequestBody List<UUID> products,
                                          @RequestParam String username) throws FeignException {
        return cartService.removeFromCart(products, username);
    }

    @Override
    public ShoppingCartDto changeProductQuantity(@RequestBody ChangeProductQuantityRequest request,
                                                 @RequestParam String username) throws FeignException {
        log.info("Пользователь {} изменяет количество товара {} на {}.",
                username,
                request.getProductId(),
                request.getQuantity());
        return cartService.changeProductQuantity(request, username);
    }
}