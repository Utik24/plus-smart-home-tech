package ru.yandex.practicum.commerce.shoppingcart.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.shoppingcart.entity.ShoppingCart;
import ru.yandex.practicum.commerce.shoppingcart.entity.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.shoppingcart.entity.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.shoppingcart.error.CartProductNotFoundException;
import ru.yandex.practicum.commerce.shoppingcart.mapper.CartMapper;
import ru.yandex.practicum.commerce.shoppingcart.repository.CartRepository;
import ru.yandex.practicum.commerce.warehouse.controller.WarehouseClient;

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
    private final WarehouseClient warehouseClient;

    public ShoppingCartDto getShoppingCart(String username) throws FeignException {
        ShoppingCart shoppingCart = getActiveCart(username);
        return cartMapper.toShoppingCartDto(shoppingCart);
    }

    private ShoppingCart getActiveCart(String username) {
        ShoppingCart shoppingCart = cartRepository.findByUsernameAndActive(username, true)
                .orElseGet(() -> createShoppingCart(username));
        if (shoppingCart.getProducts() == null) {
            shoppingCart.setProducts(new HashMap<>());
            cartRepository.save(shoppingCart);
        }
        return shoppingCart;
    }

    @Transactional
    public ShoppingCartDto addToCartProduct(String username, Map<UUID, Integer> products) throws FeignException {
        ShoppingCart shoppingCart = getActiveCart(username);
        Map<UUID, Integer> expectedProducts = new HashMap<>(shoppingCart.getProducts());
        products.forEach((productId, quantity) -> expectedProducts.merge(productId, quantity, Integer::sum));
        warehouseClient.checkProductCount(new ShoppingCartDto(shoppingCart.getShoppingCartId(), expectedProducts));
        products.forEach((k, v) -> {
            shoppingCart.getProducts().merge(k, v, Integer::sum);
        });
        cartRepository.save(shoppingCart);
        return cartMapper.toShoppingCartDto(shoppingCart);
    }

    @Transactional
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

    @Transactional
    public ShoppingCartDto changeProductQuantity(ChangeProductQuantityRequest request, String username) throws FeignException {
        ShoppingCart shoppingCart = getActiveCart(username);
        UUID productId = request.getProductId();
        Integer quantity = request.getQuantity();
        if (!shoppingCart.getProducts().containsKey(productId)) {
            throw new CartProductNotFoundException(productId);
        }
        warehouseClient.checkProductCount(new ShoppingCartDto(shoppingCart.getShoppingCartId(), Map.of(productId, quantity)));
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