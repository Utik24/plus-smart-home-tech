package ru.yandex.practicum.commerce.delivery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.commerce.delivery.dto.DeliveryDto;
import ru.yandex.practicum.commerce.delivery.entity.Delivery;
import ru.yandex.practicum.commerce.delivery.enums.DeliveryState;
import ru.yandex.practicum.commerce.delivery.mapper.AddressMapper;
import ru.yandex.practicum.commerce.delivery.repository.DeliveryRepository;
import ru.yandex.practicum.commerce.order.client.OrderClient;
import ru.yandex.practicum.commerce.order.dto.OrderDto;
import ru.yandex.practicum.commerce.warehouse.controller.WarehouseClient;
import ru.yandex.practicum.commerce.warehouse.entity.dto.AddressDto;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final AddressMapper addressMapper;
    private final OrderClient orderClient;
    private final WarehouseClient warehouseClient;

    public DeliveryDto addDelivery(DeliveryDto newDelivery) {
        Delivery delivery = addressMapper.mapToDelivery(newDelivery);

        delivery.setDeliveryState(DeliveryState.CREATED);
        deliveryRepository.save(delivery);
        return addressMapper.mapToDeliveryDto(delivery);
    }

    public void successfulDelivery(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId).orElseThrow(()-> new RuntimeException("Delivery with id " + deliveryId + " not found"));
        delivery.setDeliveryState(DeliveryState.DELIVERED);
        deliveryRepository.save(delivery);
        orderClient.completed(delivery.getOrderId());
    }

    public void pickedDelivery(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId).orElseThrow(()-> new RuntimeException("Delivery with id " + deliveryId + " not found"));
        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        deliveryRepository.save(delivery);
        orderClient.assembly(delivery.getOrderId());
    }

    public void failedDelivery(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId).orElseThrow(()-> new RuntimeException("Delivery with id " + deliveryId + " not found"));
        delivery.setDeliveryState(DeliveryState.FAILED);
        deliveryRepository.save(delivery);
        orderClient.deliveryFailed(delivery.getOrderId());
    }

    public Double getDeliveryCost(OrderDto order) {
        Delivery delivery = deliveryRepository.findById(order.getDeliveryId()).orElseThrow(()-> new RuntimeException("Delivery with id " + order.getDeliveryId() + " not found"));

        double baseRate = 5.0;
        AddressDto addressWarehouse = warehouseClient.getWarehouseAddress();
        if (addressWarehouse.getCity().contains("ADDRESS_1")){
            baseRate *= 2;
        }
        if (addressWarehouse.getCity().contains("ADDRESS_2")){
            baseRate *= 3;
        }
        if (order.getFragile()){
            baseRate *= 1.2;
        }
        baseRate += order.getDeliveryWeight() * 0.3;
        baseRate += order.getDeliveryVolume() * 0.2;

        if (!addressWarehouse.getStreet().equals(delivery.getToAddress().getStreet())){
            baseRate *= 1.2;
        }
        return baseRate;

    }
}
