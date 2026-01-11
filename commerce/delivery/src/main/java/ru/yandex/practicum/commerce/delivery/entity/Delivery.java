package ru.yandex.practicum.commerce.delivery.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.commerce.api.delivery.enums.DeliveryState;

import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "delivery")
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID deliveryId;

    @ManyToOne
    @JoinColumn(name = "from_address_id")
    private DeliveryAddress fromAddress;

    @ManyToOne
    @JoinColumn(name = "to_address_id")
    private DeliveryAddress toAddress;

    private UUID orderId;

    private DeliveryState deliveryState;
}
