package ru.yandex.practicum.commerce.delivery.client;

import feign.FeignException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.delivery.dto.DeliveryDto;
import ru.yandex.practicum.commerce.order.dto.OrderDto;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "delivery", path = "/api/v1/delivery")
public interface DeliveryClient {
    @PutMapping
    DeliveryDto addDelivery(@Valid @RequestBody DeliveryDto newDelivery) throws FeignException;

    @PostMapping("/successful")
    void successfulDelivery(@NotNull @RequestBody UUID deliveryId) throws FeignException;

    @PostMapping("/picked")
    void pickedDelivery(@NotNull @RequestBody UUID deliveryId) throws FeignException;

    @PostMapping("/failed")
    void failedDelivery(@NotNull @RequestBody UUID deliveryId) throws FeignException;
    @PostMapping("/cost")
    BigDecimal getDeliveryCost(@Valid @RequestBody OrderDto order) throws FeignException;
}
