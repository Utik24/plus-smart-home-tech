package ru.yandex.practicum.commerce.warehouse.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class BookedProducts {
    private Double deliveryWeight = 0.0;
    private Double deliveryVolume = 0.0;
    private Boolean fragile = false;
}
