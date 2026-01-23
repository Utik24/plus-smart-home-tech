package ru.yandex.practicum.commerce.warehouse.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.shoppingcart.entity.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.warehouse.entity.BookedProducts;
import ru.yandex.practicum.commerce.warehouse.entity.WarehouseProduct;
import ru.yandex.practicum.commerce.warehouse.entity.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.warehouse.entity.dto.AddressDto;
import ru.yandex.practicum.commerce.warehouse.entity.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.warehouse.entity.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.warehouse.error.exception.InsufficientProductQuantityException;
import ru.yandex.practicum.commerce.warehouse.error.exception.WarehouseProductAlreadyExistsException;
import ru.yandex.practicum.commerce.warehouse.error.exception.WarehouseProductNotFoundException;
import ru.yandex.practicum.commerce.warehouse.mapper.WarehouseMapper;
import ru.yandex.practicum.commerce.warehouse.repository.WarehouseRepository;

import java.security.SecureRandom;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseService {
    private static final String[] ADDRESSES =
            new String[]{"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS =
            ADDRESSES[Random.from(new SecureRandom()).nextInt(0, ADDRESSES.length)];
    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper warehouseMapper;


    @Transactional
    public void addProduct(NewProductInWarehouseRequest request) {
        warehouseRepository.findById(request.getProductId()).ifPresent(product -> {
            throw new WarehouseProductAlreadyExistsException(request.getProductId());
        });
        WarehouseProduct warehouseProduct = warehouseMapper.toWarehouseProduct(request);
        warehouseRepository.save(warehouseProduct);
    }

    @Transactional(readOnly = true)
    public BookedProductsDto checkProductCount(ShoppingCartDto shoppingCartDto) {
        BookedProducts bookedProducts = new BookedProducts();
        Map<UUID, WarehouseProduct> productsById = getWarehouseProducts(shoppingCartDto.getProducts().keySet());
        shoppingCartDto.getProducts().forEach((productId, quantity) -> {
            WarehouseProduct warehouseProduct = productsById.get(productId);
            if (warehouseProduct == null) {
                throw new WarehouseProductNotFoundException(productId);
            }
            long availableQuantity = warehouseProduct.getQuantity();
            if (availableQuantity < quantity) {
                throw new InsufficientProductQuantityException(productId, quantity, availableQuantity);
            }
            bookedProducts.setFragile(bookedProducts.getFragile() || warehouseProduct.getFragile());
            double volume = warehouseProduct.getWidth() * warehouseProduct.getDepth() * warehouseProduct.getHeight();
            bookedProducts.setDeliveryVolume(bookedProducts.getDeliveryVolume() + volume * quantity);
            bookedProducts.setDeliveryWeight(bookedProducts.getDeliveryWeight() + warehouseProduct.getWeight() * quantity);
        });
        return new BookedProductsDto(bookedProducts.getDeliveryWeight(), bookedProducts.getDeliveryVolume(), bookedProducts.getFragile());
    }

    @Transactional
    public void addProductQuantity(AddProductToWarehouseRequest request) {
        WarehouseProduct warehouseProduct = warehouseRepository.findById(request.getProductId())
                .orElseThrow(() -> new WarehouseProductNotFoundException(request.getProductId()));
        warehouseProduct.setQuantity(warehouseProduct.getQuantity() + request.getQuantity());
        warehouseRepository.save(warehouseProduct);
    }

    @Transactional(readOnly = true)
    public AddressDto getWarehouseAddress() {
        return new AddressDto(CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS);
    }

    private Map<UUID, WarehouseProduct> getWarehouseProducts(Collection<UUID> productIds) {
        return warehouseRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(WarehouseProduct::getProductId, Function.identity()));
    }

    @Transactional
    public void reserveProducts(Map<UUID, Integer> products) {
        Map<UUID, WarehouseProduct> productsById = getWarehouseProducts(products.keySet());
        products.forEach((productId, quantity) -> {
            WarehouseProduct warehouseProduct = productsById.get(productId);
            if (warehouseProduct == null) {
                throw new WarehouseProductNotFoundException(productId);
            }
            long availableQuantity = warehouseProduct.getQuantity();
            if (availableQuantity < quantity) {
                throw new InsufficientProductQuantityException(productId, quantity, availableQuantity);
            }
            warehouseProduct.setQuantity(availableQuantity - quantity);
        });
        warehouseRepository.saveAll(productsById.values());
    }

    public void returnProductsToWarehouse(Map<UUID, Integer> products) throws FeignException {
        List<WarehouseProduct> warehouseProducts = warehouseRepository.findAllById(products.keySet());
        if (warehouseProducts.isEmpty()) {
            return;
        }
        warehouseProducts.forEach(warehouseProduct -> {
            warehouseProduct.setQuantity(warehouseProduct.getQuantity() +
                    products.get(warehouseProduct.getProductId()));
        });
        warehouseRepository.saveAll(warehouseProducts);
    }
}
