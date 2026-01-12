package ru.yandex.practicum.commerce.delivery.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.delivery.client.DeliveryClient;
import ru.yandex.practicum.commerce.delivery.dto.DeliveryDto;
import ru.yandex.practicum.commerce.delivery.service.DeliveryService;
import ru.yandex.practicum.commerce.order.dto.OrderDto;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
public class DeliveryController implements DeliveryClient {

    private final DeliveryService deliveryService;

    @Override
    public DeliveryDto addDelivery(DeliveryDto newDelivery) {
        log.info("Запрос на создание доставки: {}", newDelivery);
        return deliveryService.addDelivery(newDelivery);
    }

    @Override
    public void successfulDelivery(UUID deliveryId) {
        log.info("Запрос при удачной доставки: {}",  deliveryId);
        deliveryService.successfulDelivery(deliveryId);
    }

    @Override
    public void pickedDelivery(UUID deliveryId) {
        log.info("Запрос при передачи заказа в доставку: {}", deliveryId);
        deliveryService.pickedDelivery(deliveryId);
    }

    @Override
    public void failedDelivery(UUID deliveryId) {
        log.info("Запрос при неудачной доставки: {}", deliveryId);
        deliveryService.failedDelivery(deliveryId);
    }

    @Override
    public BigDecimal getDeliveryCost(OrderDto order) {
        log.info("Запрос на расчет стоимости доставки: {}", order);
        return deliveryService.getDeliveryCost(order);
    }
}
