package com.example.orderservice.order;

import com.example.orderservice.dto.OrderDTO;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public Flux<OrderDTO> getAllOrders() {
        return Flux.fromIterable(orderService.getAllOrders());
    }

    @GetMapping("/{orderId}")
    public Mono<OrderDTO> getOrderById(@PathVariable UUID orderId) {
        return Mono.justOrEmpty(orderService.getOrderById(orderId));
    }

    @PostMapping
    public Mono<OrderDTO> createOrder(
            @RequestBody OrderDTO orderDTO,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        orderService.setToken(token);
        return orderService.createOrder(orderDTO);
    }

    @DeleteMapping("/{orderId}")
    public Mono<Void> deleteOrderById(@PathVariable UUID orderId) {
        return Mono.fromRunnable(() -> orderService.deleteOrderById(orderId));
    }
}