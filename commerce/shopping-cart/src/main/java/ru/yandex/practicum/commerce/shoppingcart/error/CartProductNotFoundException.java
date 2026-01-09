package ru.yandex.practicum.commerce.shoppingcart.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CartProductNotFoundException extends RuntimeException {
    public CartProductNotFoundException(UUID productId) {
        super("Товар с id " + productId + " не найден в корзине");
    }
}