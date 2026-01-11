package ru.yandex.practicum.commerce.payment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.order.dto.OrderDto;
import ru.yandex.practicum.commerce.payment.client.PaymentClient;
import ru.yandex.practicum.commerce.payment.dto.PaymentDto;
import ru.yandex.practicum.commerce.payment.service.PaymentService;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentClient {

    private final PaymentService paymentService;

    @Override
    public PaymentDto createPaymentOrder(OrderDto orderDto) {
        log.info("Запрос на создание оплаты заказа: {}", orderDto);
        return paymentService.createPaymentOrder(orderDto);
    }

    @Override
    public Double calculateTotalCost(OrderDto orderDto) {
        log.info("Запрос на рассчет стоимости продуктов: {}", orderDto);
        return paymentService.calculateTotalCost(orderDto);
    }

    @Override
    public void refundPayment(UUID paymentId) {
        log.info("Запрос при успешной оплате заказа: {}", paymentId);
        paymentService.refundPayment(paymentId);
    }

    @Override
    public Double calculateProductCost(OrderDto orderDto) {
        log.info("Запрос на рассчет стоимости продуктов: {}", orderDto);
        return paymentService.calculateProductCost(orderDto);
    }

    @Override
    public void setPaymentFailed(UUID paymentId) {
        log.info("Запрос при неудачной оплате заказа: {}", paymentId);
        paymentService.setPaymentFailed(paymentId);
    }
}
