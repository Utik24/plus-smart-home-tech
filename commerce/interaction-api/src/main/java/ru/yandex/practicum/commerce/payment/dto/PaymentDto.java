package ru.yandex.practicum.commerce.payment.dto;

import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.commerce.payment.enums.PaymentStatus;

import java.util.UUID;

@Getter
@Setter
public class PaymentDto {
    private UUID paymentId;
    private Double totalPayment;
    private Double deliveryTotal;
    private Double feeTotal;
    private Double productTotal;
    private PaymentStatus status;
}
