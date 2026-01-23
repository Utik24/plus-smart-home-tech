package ru.yandex.practicum.commerce.warehouse.entity.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class DimensionDto {
    @DecimalMin(value = "1.0", message = "width должен быть минимум 1")
    private double width;

    @DecimalMin(value = "1.0", message = "height должен быть минимум 1")
    private double height;

    @DecimalMin(value = "1.0", message = "depth должен быть минимум 1")
    private double depth;
}
