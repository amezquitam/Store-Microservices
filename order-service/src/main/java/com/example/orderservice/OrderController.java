package com.example.orderservice;

import com.example.orderservice.dto.OrderDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
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
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<OrderDTO> createOrder(@RequestBody OrderDTO orderDTO) {
        return Mono.just(orderService.createOrder(orderDTO));
    }

    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteOrderById(@PathVariable UUID orderId) {
        return Mono.fromRunnable(() -> orderService.deleteOrderById(orderId));
    }
}