package ru.yandex.practicum.commerce.payment.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.order.dto.OrderDto;
import ru.yandex.practicum.commerce.payment.dto.PaymentDto;
import ru.yandex.practicum.commerce.payment.entity.Payment;

import java.math.BigDecimal;

@Slf4j
@Component
public class PaymentMapper {
    public static Payment mapToPayment(OrderDto orderDto) {
        Payment entity = new Payment();
        entity.setOrderId(orderDto.getOrderId());
        entity.setTotalPayment(orderDto.getTotalPrice());
        entity.setDeliveryTotal(orderDto.getDeliveryPrice());
        entity.setProductTotal(orderDto.getProductPrice());
        log.info("Результат маппинга в Payment: {}", entity);
        return entity;
    }

    public static PaymentDto mapToPaymentDto(Payment entity) {
        PaymentDto dto = new PaymentDto();
        dto.setPaymentId(entity.getPaymentId());
        dto.setTotalPayment(entity.getTotalPayment());
        dto.setDeliveryTotal(entity.getDeliveryTotal());
        dto.setProductTotal(entity.getProductTotal());
        BigDecimal feeTotal = entity.getTotalPayment()
                .subtract(entity.getDeliveryTotal())
                .subtract(entity.getProductTotal());
        dto.setFeeTotal(feeTotal);
        dto.setStatus(entity.getStatus());
        log.info("Результат маппинга в PaymentDto: {}", dto);
        return dto;
    }
}