package com.example.orderservice;

import com.example.orderservice.dto.OrderDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
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
        OrderEntity orderEntity = mapToEntity(orderDTO);
        OrderEntity savedOrder = orderRepository.save(orderEntity);
        return mapToDTO(savedOrder);
    }

    @Override
    public void deleteOrderById(UUID orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new IllegalArgumentException("Order with ID " + orderId + " not found");
        }
        orderRepository.deleteById(orderId);
    }

    private OrderDTO mapToDTO(OrderEntity orderEntity) {
        return OrderDTO.builder()
                .id(orderEntity.getId())
                .orderDate(orderEntity.getOrderDate())
                .status(orderEntity.getStatus())
                .totalAmount(orderEntity.getTotalAmount())
                .build();
    }

    private OrderEntity mapToEntity(OrderDTO orderDTO) {
        return new OrderEntity(
                orderDTO.id(),
                orderDTO.orderDate(),
                orderDTO.status(),
                orderDTO.totalAmount()
        );
    }
}