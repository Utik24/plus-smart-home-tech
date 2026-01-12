package ru.yandex.practicum.commerce.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.commerce.payment.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID paymentId;

    private UUID orderId;

    private BigDecimal  totalPayment;

    private BigDecimal  deliveryTotal;

    private BigDecimal  feeTotal;

    private BigDecimal productTotal;

    private PaymentStatus status;
}
