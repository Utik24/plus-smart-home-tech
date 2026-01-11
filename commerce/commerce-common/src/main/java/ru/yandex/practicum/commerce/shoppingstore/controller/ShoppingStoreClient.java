package ru.yandex.practicum.commerce.shoppingstore.controller;

import feign.FeignException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.shoppingstore.entity.ProductCategory;
import ru.yandex.practicum.commerce.shoppingstore.entity.dto.ProductDto;
import ru.yandex.practicum.commerce.shoppingstore.enums.QuantityState;

import java.util.UUID;

@FeignClient(name = "shopping-store", url = "/api/v1/shopping-store")
public interface ShoppingStoreClient {

    @GetMapping
    Page<ProductDto> getProducts(@RequestParam ProductCategory productCategory,
                                 @PageableDefault Pageable pageable) throws FeignException;

    @PutMapping
    ProductDto addProduct(@RequestBody @Valid ProductDto productDto) throws FeignException;

    @PostMapping
    ProductDto updateProduct(@RequestBody @Valid ProductDto productDto) throws FeignException;

    @PostMapping("/removeProductFromStore")
    Boolean removeProductFromStore(@RequestBody @NotNull UUID productId) throws FeignException;

    @PostMapping("/quantityState")
    Boolean setQuantityState(@RequestParam UUID productId,
                             @RequestParam QuantityState quantityState) throws FeignException;

    @GetMapping("/{productId}")
    ProductDto getProduct(@PathVariable UUID productId) throws FeignException;

}
