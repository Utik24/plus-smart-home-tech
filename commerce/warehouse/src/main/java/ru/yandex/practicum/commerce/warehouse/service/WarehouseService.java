package ru.yandex.practicum.commerce.warehouse.service;

import feign.FeignException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.commerce.shoppingcart.entity.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.warehouse.entity.BookedProducts;
import ru.yandex.practicum.commerce.warehouse.entity.WarehouseProduct;
import ru.yandex.practicum.commerce.warehouse.entity.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.warehouse.entity.dto.AddressDto;
import ru.yandex.practicum.commerce.warehouse.entity.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.warehouse.entity.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.warehouse.mapper.WarehouseMapper;
import ru.yandex.practicum.commerce.warehouse.repository.WarehouseRepository;

import java.security.SecureRandom;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseService {
    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper warehouseMapper;

    private static final String[] ADDRESSES =
            new String[]{"ADDRESS_1", "ADDRESS_2"};

    private static final String CURRENT_ADDRESS =
            ADDRESSES[Random.from(new SecureRandom()).nextInt(0, ADDRESSES.length)];

    public void addProduct(NewProductInWarehouseRequest request) throws FeignException {
        warehouseRepository.findById(request.getProductId()).ifPresent(product -> {
            throw new ValidationException("Товар уже добавлен");
        });

        WarehouseProduct warehouseProduct = warehouseMapper.toWarehouseProduct(request);
        warehouseRepository.save(warehouseProduct);
    }

    public BookedProductsDto checkProductCount(ShoppingCartDto shoppingCartDto) throws FeignException {
        BookedProducts bookedProducts = new BookedProducts();
        shoppingCartDto.getProducts().forEach((productId, quantity) -> {
            WarehouseProduct warehouseProduct = warehouseRepository.findById(productId).orElseThrow(() -> new ValidationException("Товар не найден"));
            if (warehouseProduct.getQuantity() < quantity) {
                throw new ValidationException("Товар не найден");
            }
            bookedProducts.setFragile(bookedProducts.getFragile() || warehouseProduct.getFragile());
            bookedProducts.setDeliveryVolume(bookedProducts.getDeliveryVolume() + warehouseProduct.getWeight()
                    * warehouseProduct.getDepth() * warehouseProduct.getHeight());
            bookedProducts.setDeliveryWeight(bookedProducts.getDeliveryWeight() + warehouseProduct.getWeight() * quantity);
        });
        return new BookedProductsDto(bookedProducts.getDeliveryWeight(), bookedProducts.getDeliveryVolume(), bookedProducts.getFragile());
    }

    public void addProductQuantity(AddProductToWarehouseRequest request) throws FeignException {
        WarehouseProduct warehouseProduct = warehouseRepository.findById(request.getProductId()).orElseThrow(() -> new ValidationException("Товар не найден"));
        warehouseProduct.setQuantity(request.getQuantity());
        warehouseRepository.save(warehouseProduct);
    }

    public AddressDto getWarehouseAddress() throws FeignException {
        return new AddressDto(CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS);
    }
}
