package ru.yandex.practicum.commerce.shoppingcart.entity.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeProductQuantityRequest {
    @NotNull(message = "productId не может быть пустым.")
    private UUID productId;
    @JsonAlias("newQuantity")
    @Positive(message = "quantity должен быть больше 0.")
    private int quantity;

}
