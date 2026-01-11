package ru.yandex.practicum.commerce.payment.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.order.dto.OrderDto;
import ru.yandex.practicum.commerce.payment.dto.PaymentDto;

import java.util.UUID;

@FeignClient(name = "payment", path = "/api/v1/payment")
public interface PaymentClient {

    @PostMapping
    PaymentDto createPaymentOrder(@Valid @RequestBody OrderDto orderDto);

    @PostMapping("/totalCost")
    Double calculateTotalCost(@Valid @RequestBody OrderDto orderDto);

    @PostMapping("/refund")
    void refundPayment(@Valid @RequestBody UUID paymentId);

    @PostMapping("/productCost")
    Double calculateProductCost(@Valid @RequestBody OrderDto orderDto);

    @PostMapping("/failed")
    void setPaymentFailed(@Valid @RequestBody UUID paymentId);

}
