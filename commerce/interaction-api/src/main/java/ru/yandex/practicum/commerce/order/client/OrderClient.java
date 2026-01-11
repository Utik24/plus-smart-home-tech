package ru.yandex.practicum.commerce.order.client;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.order.dto.OrderDto;
import ru.yandex.practicum.commerce.order.dto.ProductReturnRequest;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "order", path = "/api/v1/order")
public interface OrderClient {

    @GetMapping
    List<OrderDto> getOrders(@Valid @RequestParam @NotEmpty String username);

    @PutMapping
    OrderDto createOrder(@Valid @RequestParam @NotEmpty String username, @RequestBody OrderDto newOrder);

    @PostMapping("/return")
    OrderDto returnProducts(@Valid @RequestBody ProductReturnRequest request);

    @PostMapping("/payment")
    OrderDto payment(@Valid @RequestBody UUID orderId);

    @PostMapping("/failed")
    OrderDto paymentFailed(@Valid @RequestBody UUID orderId);

    @PostMapping("/delivery")
    OrderDto delivery(@Valid @RequestBody UUID orderId);

    @PostMapping("/delivery/failed")
    OrderDto deliveryFailed(@Valid @RequestBody UUID orderId);

    @PostMapping("/completed")
    OrderDto completed(@Valid @RequestBody UUID orderId);

    @PostMapping("/calculate/total")
    OrderDto calculateTotal(@Valid @RequestBody UUID orderId);

    @PostMapping("/calculate/delivery")
    OrderDto calculateDelivery(@Valid @RequestBody UUID orderId);

    @PostMapping("/assembly")
    OrderDto assembly(@Valid @RequestBody UUID orderId);

    @PostMapping("/assembly/failed")
    OrderDto assemblyFailed(@Valid @RequestBody UUID orderId);

}
