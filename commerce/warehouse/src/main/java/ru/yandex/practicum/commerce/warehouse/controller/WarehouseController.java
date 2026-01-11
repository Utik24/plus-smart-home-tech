package ru.yandex.practicum.commerce.warehouse.controller;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.shoppingcart.entity.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.warehouse.entity.OrderBooking;
import ru.yandex.practicum.commerce.warehouse.entity.dto.*;
import ru.yandex.practicum.commerce.warehouse.service.WarehouseOrderService;
import ru.yandex.practicum.commerce.warehouse.service.WarehouseService;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController implements WarehouseClient {

    private final WarehouseService warehouseService;
    private final WarehouseOrderService warehouseOrderService;


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


    @Override
    public void shippedWarehouse(ShippedToDeleveryRequest request) throws FeignException {

    }

    @Override
    public void returnProductsToWarehouse(Map<UUID, Integer> products) throws FeignException {
        warehouseService.returnProductsToWarehouse(products);
    }

    @Override
    public BookedProductsDto assembleProducts(AssemblyProductsForOrderRequest request) throws FeignException {
        ShoppingCartDto shoppingCartDto = new ShoppingCartDto(request.getOrderId(), request.getProducts());
        BookedProductsDto bookedProductsDto = checkProductCount(shoppingCartDto);

        OrderBooking orderBooking = new OrderBooking();
        orderBooking.setOrderId(request.getOrderId());
        orderBooking.setProducts(request.getProducts());
        warehouseOrderService.save(orderBooking);

        return bookedProductsDto;
    }
}
