package com.example.orderservice.order;

import com.example.orderservice.dto.OrderDTO;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;

    public OrderServiceImpl(OrderRepository orderRepository, RestTemplate restTemplate) {
        this.orderRepository = orderRepository;
        this.restTemplate = restTemplate;
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
    public OrderDTO createOrder(OrderDTO orderDTO) {
        Order orderEntity = mapToEntity(orderDTO);
        orderEntity.getOrderProducts().forEach(product -> {
            URI uri = URI.create("lb://inventory-service/api/inventory/" + product.getProductId() +
                    "/decrement?amount=" + product.getQuantity());

            ResponseExtractor<Boolean> responseExtractor = (response) ->
                response.getStatusCode() == HttpStatusCode.valueOf(200);
            try {
                restTemplate.execute(uri, HttpMethod.PATCH, null, responseExtractor);
            } catch (RestClientException ex) {
                throw new RuntimeException("from inventory:" + ex.getMessage());
            }
        });
        Order savedOrder = orderRepository.save(orderEntity);
        return mapToDTO(savedOrder);
    }

    @Override
    public void deleteOrderById(UUID orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new IllegalArgumentException("Order with ID " + orderId + " not found");
        }
        orderRepository.deleteById(orderId);
    }

    private OrderDTO mapToDTO(Order orderEntity) {
        return new OrderDTO(orderEntity.getId(), orderEntity.getTimestamp(), orderEntity.getOrderProducts());
    }

    private Order mapToEntity(OrderDTO orderDTO) {
        return new Order(
                orderDTO.id(),
                orderDTO.orderDate(),
                orderDTO.orderProducts()
        );
    }
}