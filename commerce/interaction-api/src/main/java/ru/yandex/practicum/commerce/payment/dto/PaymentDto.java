package ru.yandex.practicum.commerce.payment.dto;

import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.commerce.payment.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class PaymentDto {
    private UUID paymentId;
    private BigDecimal totalPayment;
    private BigDecimal deliveryTotal;
    private BigDecimal feeTotal;
    private BigDecimal productTotal;
    private PaymentStatus status;
}
