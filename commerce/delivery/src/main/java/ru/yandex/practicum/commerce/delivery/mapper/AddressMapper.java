package ru.yandex.practicum.commerce.delivery.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.delivery.dto.DeliveryDto;
import ru.yandex.practicum.commerce.delivery.entity.Delivery;
import ru.yandex.practicum.commerce.delivery.entity.DeliveryAddress;
import ru.yandex.practicum.commerce.warehouse.entity.dto.AddressDto;

@Component
public class AddressMapper {
    public static DeliveryAddress mapToAddress(AddressDto dto) {
        DeliveryAddress address = new DeliveryAddress();
        address.setCountry(dto.getCountry());
        address.setCity(dto.getCity());
        address.setStreet(dto.getStreet());
        address.setHouse(dto.getHouse());
        address.setFlat(dto.getFlat());
        return address;
    }

    public static AddressDto mapToAddressDto(DeliveryAddress address) {
        AddressDto dto = new AddressDto(
        address.getCountry(),
        address.getCity(),
        address.getStreet(),
        address.getHouse(),
        address.getFlat()        );
        return dto;
    }

    public static Delivery mapToDelivery(DeliveryDto dto) {
        Delivery delivery = new Delivery();
        delivery.setDeliveryId(dto.getDeliveryId());
        delivery.setFromAddress(mapToAddress(dto.getFromAddress()));
        delivery.setToAddress(mapToAddress(dto.getToAddress()));
        delivery.setOrderId(dto.getOrderId());
        delivery.setDeliveryState(dto.getDeliveryState());
        return delivery;
    }

    public static DeliveryDto mapToDeliveryDto(Delivery delivery) {
        DeliveryDto dto = new DeliveryDto();
        dto.setDeliveryId(delivery.getDeliveryId());
        dto.setFromAddress(mapToAddressDto(delivery.getFromAddress()));
        dto.setToAddress(mapToAddressDto(delivery.getToAddress()));
        dto.setOrderId(delivery.getOrderId());
        dto.setDeliveryState(delivery.getDeliveryState());
        return dto;
    }
}
