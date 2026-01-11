package ru.yandex.practicum.commerce.shoppingstore.controller;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.shoppingstore.entity.ProductCategory;
import ru.yandex.practicum.commerce.shoppingstore.entity.dto.ProductDto;
import ru.yandex.practicum.commerce.shoppingstore.enums.QuantityState;
import ru.yandex.practicum.commerce.shoppingstore.service.ProductService;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/shopping-store")
@RequiredArgsConstructor
public class ShoppingStoreController implements ShoppingStoreClient {

    private final ProductService productService;

    @Override
    public Page<ProductDto> getProducts(@RequestParam(name = "category") ProductCategory productCategory, Pageable pageable) throws FeignException {
        log.info("Получение продуктов по категории {} по странице ", productCategory, pageable);
        return productService.getProductsByCategory(productCategory, pageable);
    }

    @Override
    public ProductDto addProduct(ProductDto productDto) throws FeignException {
        log.info("Добавление продукта {}", productDto);
        return productService.addProduct(productDto);
    }

    @Override
    public ProductDto updateProduct(ProductDto productDto) throws FeignException {
        log.info("Обновление продукта {}", productDto);
        return productService.updateProduct(productDto);
    }

    @Override
    public Boolean removeProductFromStore(UUID productId) throws FeignException {
        log.info("Удаляем продукт {}", productId);
        return productService.removeProductFromStore(productId);
    }

    @Override
    public Boolean setQuantityState(UUID productId, QuantityState quantityState) throws FeignException {
        log.info("Обновляем quantity {} для продукта {}", quantityState, productId);
        return productService.setQuantityState(productId, quantityState);
    }

    @Override
    public ProductDto getProduct(UUID productId) throws FeignException {
        log.info("Покажи продукт с ID: {}", productId);
        return productService.getProductById(productId);
    }
}
