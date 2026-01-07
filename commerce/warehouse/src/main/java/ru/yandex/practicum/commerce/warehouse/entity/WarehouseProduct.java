package ru.yandex.practicum.commerce.warehouse.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "warehouse")
@RequiredArgsConstructor
public class WarehouseProduct {
    @Id
    private UUID productId;

    private Boolean fragile;
    private Double width;
    private Double height;
    private Double depth;
    private Double weight;
    private Long quantity;
}
