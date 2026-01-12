package ru.yandex.practicum.commerce.order.client;

import feign.FeignException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.order.dto.OrderDto;
import ru.yandex.practicum.commerce.order.dto.ProductReturnRequest;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "order", path = "/api/v1/order")
public interface OrderClient {

    @GetMapping
    List<OrderDto> getOrders(@RequestParam @NotEmpty String username) throws FeignException;

    @PutMapping
    OrderDto createOrder(@RequestParam @NotEmpty String username, @Valid @RequestBody OrderDto newOrder)
            throws FeignException;

    @PostMapping("/return")
    OrderDto returnProducts(@Valid @RequestBody ProductReturnRequest request) throws FeignException;

    @PostMapping("/payment/success")
    OrderDto paymentSuccess(@NotNull @RequestBody UUID orderId) throws FeignException;

    @PostMapping("/payment")
    OrderDto payment(@NotNull @RequestBody UUID orderId) throws FeignException;

    @PostMapping("/failed")
    OrderDto paymentFailed(@NotNull @RequestBody UUID orderId) throws FeignException;

    @PostMapping("/delivery")
    OrderDto delivery(@NotNull @RequestBody UUID orderId) throws FeignException;

    @PostMapping("/delivery/failed")
    OrderDto deliveryFailed(@NotNull @RequestBody UUID orderId) throws FeignException;

    @PostMapping("/completed")
    OrderDto completed(@NotNull @RequestBody UUID orderId) throws FeignException;

    @PostMapping("/calculate/total")
    OrderDto calculateTotal(@NotNull @RequestBody UUID orderId) throws FeignException;

    @PostMapping("/calculate/delivery")
    OrderDto calculateDelivery(@NotNull @RequestBody UUID orderId) throws FeignException;

    @PostMapping("/assembly")
    OrderDto assembly(@NotNull @RequestBody UUID orderId) throws FeignException;

    @PostMapping("/assembly/failed")
    OrderDto assemblyFailed(@NotNull @RequestBody UUID orderId) throws FeignException;

}
