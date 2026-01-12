package ru.yandex.practicum.commerce.order.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.order.client.OrderClient;
import ru.yandex.practicum.commerce.order.dto.OrderDto;
import ru.yandex.practicum.commerce.order.dto.ProductReturnRequest;
import ru.yandex.practicum.commerce.order.service.OrderService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OrderController implements OrderClient {

    private final OrderService orderService;

    @Override
    public List<OrderDto> getOrders(String username) {
        log.info("Запрос на получение заказов пользователя: {}", username);
        return orderService.getOrder(username);
    }

    @Override
    public OrderDto createOrder(String username, OrderDto newOrder) {
        log.info("Запрос пользователя: {} на создание нового заказа: {}", username, newOrder);
        return orderService.createOrder(username, newOrder);
    }

    @Override
    public OrderDto returnProducts(ProductReturnRequest request) {
        log.info("Запрос на возврат заказа: {}", request);
        return orderService.returnProducts(request);
    }

    @Override
    public OrderDto payment(UUID orderId) {
        log.info("Запрос на оплату заказа: {}", orderId);
        return orderService.payment(orderId);
    }

    @Override
    public OrderDto paymentSuccess(UUID orderId) {
        log.info("Запрос при успешной оплате заказа: {}", orderId);
        return orderService.paymentSuccess(orderId);
    }

    @Override
    public OrderDto paymentFailed(UUID orderId) {
        log.info("Запрос при неудачной оплате заказа: {}", orderId);
        return orderService.paymentFailed(orderId);
    }

    @Override
    public OrderDto delivery(UUID orderId) {
        log.info("Запрос при удачной доставке заказа: {}", orderId);
        return orderService.delivery(orderId);
    }

    @Override
    public OrderDto deliveryFailed(UUID orderId) {
        log.info("Запрос при неудачной доставке заказа: {}", orderId);
        return orderService.deliveryFailed(orderId);
    }

    @Override
    public OrderDto completed(UUID orderId) {
        log.info("Запрос на завершение заказа: {}", orderId);
        return orderService.completed(orderId);
    }

    @Override
    public OrderDto calculateTotal(UUID orderId) {
        log.info("Запрос на рассчет полной стоимости заказа: {}", orderId);
        return orderService.calculateTotal(orderId);
    }

    @Override
    public OrderDto calculateDelivery(UUID orderId) {
        log.info("Запрос на рассчет стоимости доставки: {}", orderId);
        return orderService.calculateDelivery(orderId);
    }

    @Override
    public OrderDto assembly(UUID orderId) {
        log.info("Запрос на сбор заказа на складе: {}", orderId);
        return orderService.assembly(orderId);
    }

    @Override
    public OrderDto assemblyFailed(UUID orderId) {
        log.info("Запрос при неудачной сборке заказа на складе: {}", orderId);
        return orderService.assemblyFailed(orderId);
    }
}
