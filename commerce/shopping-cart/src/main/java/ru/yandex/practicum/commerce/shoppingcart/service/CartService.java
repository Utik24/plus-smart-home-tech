package ru.yandex.practicum.commerce.shoppingcart.service;

import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.shoppingcart.entity.ShoppingCart;
import ru.yandex.practicum.commerce.shoppingcart.entity.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.shoppingcart.entity.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.shoppingcart.mapper.CartMapper;
import ru.yandex.practicum.commerce.shoppingcart.repository.CartRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartMapper cartMapper;

    public ShoppingCartDto getShoppingCart(String username) throws FeignException {

        ShoppingCart shoppingCart = getActiveCart(username);
        return cartMapper.toShoppingCartDto(shoppingCart);
    }

    private ShoppingCart getActiveCart(String username) {
        return cartRepository.findByUsernameAndActive(username, true)
                .orElseGet(() -> createShoppingCart(username));
    }

    @Transactional
    public ShoppingCartDto addToCartProduct(String username, Map<UUID, Integer> products) throws FeignException {
        ShoppingCart shoppingCart = getActiveCart(username);
        products.forEach((k, v) -> {
            shoppingCart.getProducts().merge(k, v, Integer::sum);
        });
        cartRepository.save(shoppingCart);
        return cartMapper.toShoppingCartDto(shoppingCart);
    }

    public void deleteCart(String username) throws FeignException {
        ShoppingCart shoppingCart = getActiveCart(username);
        shoppingCart.setActive(false);
        cartRepository.save(shoppingCart);
    }

    @Transactional
    public ShoppingCartDto removeFromCart(List<UUID> products, String username) throws FeignException {
        ShoppingCart shoppingCart = getActiveCart(username);
        products.forEach(p -> {shoppingCart.getProducts().remove(p);});
        cartRepository.save(shoppingCart);
        return cartMapper.toShoppingCartDto(shoppingCart);
    }

    public ShoppingCartDto changeProductQuantity(ChangeProductQuantityRequest request, String username) throws FeignException {
        ShoppingCart shoppingCart = getActiveCart(username);
        UUID productId = request.getProductId();
        Integer quantity = request.getQuantity();
        if (!shoppingCart.getProducts().containsKey(productId)) {
            throw new RuntimeException("Продукт не найден");
        }
        shoppingCart.getProducts().put(productId, quantity);
        cartRepository.save(shoppingCart);
        return cartMapper.toShoppingCartDto(shoppingCart);
    }


    private ShoppingCart createShoppingCart(String username) {
        ShoppingCart newShoppingCart = ShoppingCart.builder()
                .username(username)
                .active(true)
                .products(new HashMap<>())
                .build();
        return cartRepository.save(newShoppingCart);
    }

}
