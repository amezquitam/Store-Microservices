package com.example.orderservice.order;

import com.example.orderservice.dto.OrderDTO;
import com.example.orderservice.dto.OrderProductDTO;
import com.example.orderservice.order_products.OrderProduct;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;

    private String token;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
        this.webClient = WebClient.builder()
            .baseUrl("http://inventory-service:8080/api/inventories/")
            .build();
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<OrderDTO> getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .map(this::mapToDTO);
    }

    @Override
    public Mono<OrderDTO> createOrder(OrderDTO orderDTO) {
        Order orderEntity = mapToEntity(orderDTO);

        return Flux.fromIterable(orderDTO.orderProducts())
            .flatMap((OrderProductDTO product) -> {
                return webClient.patch()
                    .uri(product.productId() + "/decrement?amount=" + product.quantity())
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .bodyToMono(String.class);
                }
            )
            .all(response -> response.contains("successfull"))
            .flatMap(allSuccessful -> {
                if (!allSuccessful) {
                    return Mono.error(new RuntimeException("Error al decrementar inventario"));
                }
                return Mono.fromCallable(() -> orderRepository.save(orderEntity))
                        .map(this::mapToDTO);
            });
    }

    @Override
    public void deleteOrderById(UUID orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new IllegalArgumentException("Order with ID " + orderId + " not found");
        }
        orderRepository.deleteById(orderId);
    }

    private OrderDTO mapToDTO(Order orderEntity) {
        List<OrderProductDTO> productDTOs = orderEntity.getOrderProducts().stream()
                .map(op -> new OrderProductDTO(orderEntity.getId(), op.getProductId(), op.getQuantity()))
                .collect(Collectors.toList());

        return new OrderDTO(orderEntity.getId(), orderEntity.getTimestamp(), productDTOs);
    }


    private Order mapToEntity(OrderDTO orderDTO) {
        Order order = new Order();
        order.setTimestamp(orderDTO.orderDate() != null ? orderDTO.orderDate() : LocalDateTime.now());

        List<OrderProduct> products = orderDTO.orderProducts().stream().map(p -> {
            OrderProduct op = new OrderProduct();
            op.setId(UUID.randomUUID()); // Generas el ID
            op.setProductId(p.productId());
            op.setQuantity(p.quantity());
            op.setOrder(order); // Muy importante para establecer la relación bidireccional
            return op;
        }).collect(Collectors.toList());

        order.setOrderProducts(products);
        return order;
    }


    @Override
    public void setToken(String token) {
        this.token = token;
    }


}