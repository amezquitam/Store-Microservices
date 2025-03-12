package com.example.orderservice;

import com.example.orderservice.dto.OrderDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService; // Assume there's an implementation class `OrderServiceImpl`

    private OrderEntity orderEntity;
    private OrderDTO orderDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Sample order data
        UUID orderId = UUID.randomUUID();
        LocalDateTime orderDate = LocalDateTime.now();
        String status = "PENDING";
        Double totalAmount = 100.0;

        orderEntity = new OrderEntity(orderId, orderDate, status, totalAmount);
        orderDTO = new OrderDTO(orderId, orderDate, status, totalAmount);
    }

    @Test
    void shouldReturnAllOrders_whenGetAllOrdersIsCalled() {
        // Given
        when(orderRepository.findAll()).thenReturn(List.of(orderEntity));

        // When
        List<OrderDTO> result = orderService.getAllOrders();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(orderEntity.getId(), result.get(0).id());
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnOrderById_whenOrderExists() {
        // Given
        UUID orderId = orderEntity.getId();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderEntity));

        // When
        Optional<OrderDTO> result = orderService.getOrderById(orderId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(orderId, result.get().id());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void shouldReturnEmptyOptional_whenOrderDoesNotExist() {
        // Given
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When
        Optional<OrderDTO> result = orderService.getOrderById(orderId);

        // Then
        assertFalse(result.isPresent());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void shouldCreateOrder_whenValidOrderIsGiven() {
        // Given
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(orderEntity);

        // When
        OrderDTO result = orderService.createOrder(orderDTO);

        // Then
        assertNotNull(result);
        assertEquals(orderEntity.getId(), result.id());
        verify(orderRepository, times(1)).save(any(OrderEntity.class));
    }

    @Test
    void shouldDeleteOrder_whenOrderExistsById() {
        // Given
        UUID orderId = orderEntity.getId();
        doNothing().when(orderRepository).deleteById(orderId);
        when(orderRepository.existsById(orderId)).thenReturn(true);

        // When
        orderService.deleteOrderById(orderId);

        // Then
        verify(orderRepository, times(1)).deleteById(orderId);
    }

    @Test
    void shouldNotThrowError_whenDeletingNonExistentOrder() {
        // Given
        UUID orderId = UUID.randomUUID();
        doNothing().when(orderRepository).deleteById(orderId);
        when(orderRepository.existsById(orderId)).thenReturn(true);

        // When
        orderService.deleteOrderById(orderId);

        // Then
        verify(orderRepository, times(1)).deleteById(orderId);
    }

    @Test
    void shouldThrowException_whenSavingOrderFails() {
        // Given
        when(orderRepository.save(any(OrderEntity.class))).thenThrow(new RuntimeException("Database is down"));

        // When / Then
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> orderService.createOrder(orderDTO));
        assertEquals("Database is down", thrown.getMessage());
        verify(orderRepository, times(1)).save(any(OrderEntity.class));
    }

    @Test
    void shouldHandleEmptyList_whenNoOrdersExist() {
        // Given
        when(orderRepository.findAll()).thenReturn(List.of());

        // When
        List<OrderDTO> result = orderService.getAllOrders();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository, times(1)).findAll();
    }
}