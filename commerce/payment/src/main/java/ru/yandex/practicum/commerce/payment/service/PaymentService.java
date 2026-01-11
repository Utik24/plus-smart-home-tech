package ru.yandex.practicum.commerce.payment.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.commerce.order.client.OrderClient;
import ru.yandex.practicum.commerce.order.dto.OrderDto;
import ru.yandex.practicum.commerce.payment.dto.PaymentDto;
import ru.yandex.practicum.commerce.payment.entity.Payment;
import ru.yandex.practicum.commerce.payment.enums.PaymentStatus;
import ru.yandex.practicum.commerce.payment.mapper.PaymentMapper;
import ru.yandex.practicum.commerce.payment.repository.PaymentRepository;
import ru.yandex.practicum.commerce.shoppingstore.controller.ShoppingStoreClient;
import ru.yandex.practicum.commerce.shoppingstore.dto.ProductDto;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final ShoppingStoreClient shoppingStoreClient;
    private final OrderClient orderClient;

    public PaymentDto createPaymentOrder(OrderDto orderDto) {
        if (orderDto.getTotalPrice() == null || orderDto.getDeliveryPrice() == null || orderDto.getProductPrice() == null) {
            throw new RuntimeException("Недостаточно данных для оплаты заказа");
        }
        Payment payment = paymentMapper.mapToPayment(orderDto);
        Optional<Payment> oldPayment = paymentRepository.findByOrderId(orderDto.getOrderId());
        if (oldPayment.isPresent()) {
            log.info("Старая сущность Payment: {}", oldPayment.get());
            payment.setPaymentId(oldPayment.get().getPaymentId());
        }

        payment.setStatus(PaymentStatus.PENDING);
        payment = paymentRepository.save(payment);
        log.info("Сохранили платеж в БД: {}", payment);

        return PaymentMapper.mapToPaymentDto(payment);
    }

    public Double calculateProductCost(OrderDto orderDto) {
        Map<UUID, Integer> products = orderDto.getProducts();
        if (products == null || products.isEmpty()) {
            throw new IllegalArgumentException("Список продуктов не должен быть null или пустым");
        }
        Double productCost = 0.0;
        for (UUID productId : products.keySet()) {
            ProductDto product;
            try {
                product = shoppingStoreClient.getProduct(productId);
                log.info("Находим продукт в магазине: {}", product);
            } catch (FeignException e) {
                throw new RuntimeException(e.getMessage());
            }
            productCost += product.getPrice() * products.get(productId);
        }
        log.info("Стоимость продуктов в заказе: {}", productCost);
        return productCost;
    }

    public Double calculateTotalCost(OrderDto orderDto) {
        if (orderDto.getProductPrice() == null || orderDto.getDeliveryPrice() == null) {
            throw new RuntimeException("Недостаточно данных для расчета полной стоимости заказа");
        }
        Double totalCost = orderDto.getProductPrice() * 1.1 + orderDto.getDeliveryPrice();
        log.info("Итоговая стоимость заказа: {}", totalCost);
        return totalCost;
    }


    public void setPaymentFailed(UUID paymentId) {
        if (paymentId == null) {
            throw new IllegalArgumentException("Id платежа не может быть null");
        }
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Не найден платеж с Id: " + paymentId));
        log.info("Находим нужный платеж: {}", payment);
        payment.setStatus(PaymentStatus.FAILED);

        try {
            OrderDto dto = orderClient.paymentFailed(payment.getOrderId());
            log.info("Обновляем статус заказа: {}", dto);
        } catch (FeignException e) {
            throw new RuntimeException(e.getMessage());
        }
        payment = paymentRepository.save(payment);
        log.info("Обновляем статус платежа: {}", payment);
    }

    public void refundPayment(UUID paymentId) {
        if (paymentId == null) {
            throw new IllegalArgumentException("Id платежа не может быть null");
        }
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Не найден платеж с Id: " + paymentId));
        log.info("Находим нужный платеж: {}", payment);
        payment.setStatus(PaymentStatus.SUCCESS);

        try {
            OrderDto dto = orderClient.payment(payment.getOrderId());
            log.info("Обновляем статус заказа: {}", dto);
        } catch (FeignException e) {
            throw new RuntimeException(e.getMessage());
        }
        payment = paymentRepository.save(payment);
        log.info("Обновляем статус платежа: {}", payment);
    }

}
