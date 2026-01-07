package ru.yandex.practicum.commerce.warehouse.controller;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.shoppingcart.entity.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.warehouse.entity.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.warehouse.entity.dto.AddressDto;
import ru.yandex.practicum.commerce.warehouse.entity.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.warehouse.entity.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.warehouse.service.WarehouseService;

@Slf4j
@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController implements WarehouseClient {

    private final WarehouseService warehouseService;

    @Override
    public void addProduct(NewProductInWarehouseRequest request) throws FeignException {
        warehouseService.addProduct(request);
    }

    @Override
    public BookedProductsDto checkProductCount(ShoppingCartDto shoppingCartDto) throws FeignException {
        return warehouseService.checkProductCount(shoppingCartDto);
    }

    @Override
    public void addProductQuantity(AddProductToWarehouseRequest request) throws FeignException {
        warehouseService.addProductQuantity(request);
    }

    @Override
    public AddressDto getWarehouseAddress() throws FeignException {
        return warehouseService.getWarehouseAddress();
    }
}
