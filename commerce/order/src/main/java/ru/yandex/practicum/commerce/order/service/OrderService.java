package ru.yandex.practicum.commerce.order.service;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.delivery.client.DeliveryClient;
import ru.yandex.practicum.commerce.order.dto.OrderDto;
import ru.yandex.practicum.commerce.order.dto.ProductReturnRequest;
import ru.yandex.practicum.commerce.order.entity.Order;
import ru.yandex.practicum.commerce.order.enums.OrderState;
import ru.yandex.practicum.commerce.order.mapper.OrderMapper;
import ru.yandex.practicum.commerce.order.repository.OrderRepository;
import ru.yandex.practicum.commerce.payment.client.PaymentClient;
import ru.yandex.practicum.commerce.payment.dto.PaymentDto;
import ru.yandex.practicum.commerce.shoppingcart.entity.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.warehouse.controller.WarehouseClient;
import ru.yandex.practicum.commerce.warehouse.entity.dto.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.warehouse.entity.dto.BookedProductsDto;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final WarehouseClient warehouseClient;
    private final OrderMapper orderMapper;
    private final PaymentClient paymentClient;
    private final DeliveryClient deliveryClient;

    public List<OrderDto> getOrder(String username){
        log.info("Получить заказы пользователя {}", username);
        return orderRepository.findAllByUsername(username);
    }

    public OrderDto createOrder(String username, @RequestBody OrderDto newOrder) {
        BookedProductsDto bookedProducts = warehouseClient.checkProductCount(
                new ShoppingCartDto(newOrder.getShoppingCartId(), newOrder.getProducts())
        );

        log.info("Products зарезервированы успешно: {}", bookedProducts);

        Order order = orderMapper.toEntity(newOrder);
        order.setUsername(username);
        order.setState(OrderState.NEW);

        Order savedOrder = orderRepository.save(order);
        log.info("Order создан с ID: {}", savedOrder.getOrderId());

        return orderMapper.toDto(savedOrder);
    }

    public OrderDto returnProducts(ProductReturnRequest request) {
        Order order = findOrderById(request.getOrderId());
        request.getProducts().forEach((productId, quantity) -> {
            int diffQuantity = (int) (order.getProducts().get(productId) - quantity);
            if (diffQuantity <= 0) {order.getProducts().remove(productId);} else {
                order.getProducts().put(productId, diffQuantity);
            }
        });
        order.setState(OrderState.PRODUCT_RETURNED);
        orderRepository.save(order);
        warehouseClient.returnProductsToWarehouse(order.getProducts());
        return orderMapper.toDto(order);
    }

    public OrderDto payment (UUID orderId) {
        Order order = findOrderById(orderId);
        order.setState(OrderState.ON_PAYMENT);
        if (order.getProductPrice() == null) {
            Double productCost = paymentClient.calculateProductCost(orderMapper.toDto(order));
            order.setProductPrice(productCost);
        }

        if (order.getDeliveryPrice() == null) {
            Double deliveryCost = deliveryClient.getDeliveryCost(orderMapper.toDto(order));
            order.setDeliveryPrice(deliveryCost);
        }

        if (order.getTotalPrice() == null) {
            Double totalCost = paymentClient.calculateTotalCost(orderMapper.toDto(order));
            order.setTotalPrice(totalCost);
        }
        OrderDto orderDto = orderMapper.toDto(order);
        PaymentDto paymentDto = paymentClient.createPaymentOrder(orderDto);
        order.setPaymentId(paymentDto.getPaymentId());
        order.setProductPrice(paymentDto.getProductTotal());
        order.setTotalPrice(paymentDto.getTotalPayment());
        order.setDeliveryPrice(paymentDto.getDeliveryTotal());
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    public OrderDto paymentSuccess(UUID orderId) {
        Order order = findOrderById(orderId);
        order.setState(OrderState.PAID);
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    public OrderDto paymentFailed(UUID orderId) {
        Order order = findOrderById(orderId);
        order.setState(OrderState.PAYMENT_FAILED);
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    public OrderDto delivery(UUID orderId) {
        Order order = findOrderById(orderId);
        order.setState(OrderState.DELIVERED);
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    public OrderDto deliveryFailed(UUID orderId) {
        Order order = findOrderById(orderId);
        order.setState(OrderState.DELIVERY_FAILED);
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    public OrderDto completed(UUID orderId) {
        Order order = findOrderById(orderId);
        order.setState(OrderState.COMPLETED);
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    public OrderDto calculateTotal(UUID orderId) {
        Order order = findOrderById(orderId);
        if (order.getProductPrice() == null) {
            Double productCost = paymentClient.calculateProductCost(orderMapper.toDto(order));
            order.setProductPrice(productCost);
        }
        if (order.getDeliveryPrice() == null) {
            Double deliveryCost = deliveryClient.getDeliveryCost(orderMapper.toDto(order));
            order.setDeliveryPrice(deliveryCost);
        }
        Double total = paymentClient.calculateTotalCost(orderMapper.toDto(order));
        order.setTotalPrice(total);
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    public OrderDto calculateDelivery(UUID orderId) {
        Order order = findOrderById(orderId);
        Double deliveryTotal = deliveryClient.getDeliveryCost(orderMapper.toDto(order));
        order.setDeliveryPrice(deliveryTotal);
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    public OrderDto assembly(UUID orderId) {
        Order order = findOrderById(orderId);
        if (OrderState.ASSEMBLED.equals(order.getState())) {
            return orderMapper.toDto(order);
        }
        AssemblyProductsForOrderRequest request = new AssemblyProductsForOrderRequest(orderId, order.getProducts());

        BookedProductsDto bookedProducts = warehouseClient.assembleProducts(request);
        order.setState(OrderState.ASSEMBLED);
        order.setDeliveryWeight(bookedProducts.getDeliveryWeight());
        order.setFragile(bookedProducts.isFragile());
        order.setDeliveryVolume(bookedProducts.getDeliveryVolume());
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    public OrderDto assemblyFailed(UUID orderId) {
        Order order = findOrderById(orderId);
        order.setState(OrderState.ASSEMBLY_FAILED);
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }


    private Order findOrderById(UUID orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Заказ с id "+orderId+" не найден "));
    }
}
