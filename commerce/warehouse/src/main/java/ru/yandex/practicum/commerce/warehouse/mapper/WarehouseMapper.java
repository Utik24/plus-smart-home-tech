package ru.yandex.practicum.commerce.warehouse.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.warehouse.entity.WarehouseProduct;
import ru.yandex.practicum.commerce.warehouse.entity.dto.NewProductInWarehouseRequest;

@Component
public class WarehouseMapper {
    public WarehouseProduct toWarehouseProduct(NewProductInWarehouseRequest request) {
        if (request == null) {
            return null;
        }

        WarehouseProduct warehouseProduct = new WarehouseProduct();
        warehouseProduct.setProductId(request.getProductId());
        warehouseProduct.setFragile(request.isFragile());
        warehouseProduct.setWeight(request.getWeight());
        if (request.getDimension() != null) {
            warehouseProduct.setWidth(request.getDimension().getWidth());
            warehouseProduct.setHeight(request.getDimension().getHeight());
            warehouseProduct.setDepth(request.getDimension().getDepth());
        }
        warehouseProduct.setQuantity(0L);
        return warehouseProduct;
    }
}