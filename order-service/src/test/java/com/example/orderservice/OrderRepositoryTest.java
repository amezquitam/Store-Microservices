package com.example.orderservice;

import com.example.orderservice.order.Order;
import com.example.orderservice.order.OrderRepository;
import com.example.orderservice.order_products.OrderProduct;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@DataJpaTest
@SpringJUnitConfig
class OrderRepositoryTest {

    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15.4-alpine").withDatabaseName("order_db").withUsername("test").withPassword("test");
    
    @Autowired
    private OrderRepository orderRepository;

    private Order orderEntity;

    @BeforeEach
    void setUp() {
        orderEntity = new Order();
        orderEntity.setTimestamp(LocalDateTime.now());
        orderEntity = orderRepository.save(orderEntity);
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
    }

    @DynamicPropertySource
    static void properties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @Test
    void givenOrderEntity_whenSaved_thenItExistsInDatabase() {
        // then
        assertThat(orderEntity).isNotNull();
        assertThat(orderEntity.getId()).isNotNull();
        assertThat(orderRepository.findAll()).hasSize(1);
    }

    @Test
    void givenOrderId_whenFindById_thenReturnOrderEntity() {
        // when
        Optional<Order> foundOrder = orderRepository.findById(orderEntity.getId());

        // then
        assertThat(foundOrder).isPresent();
    }

    @Test
    void givenNonExistentOrderId_whenFindById_thenReturnEmpty() {
        // when
        Optional<Order> foundOrder = orderRepository.findById(UUID.randomUUID());

        // then
        assertThat(foundOrder).isNotPresent();
    }

    @Test
    void givenOrderEntity_whenDeleted_thenOrderDoesNotExist() {
        // given
        UUID orderId = orderEntity.getId();

        // when
        orderRepository.deleteById(orderId);

        // then
        assertThat(orderRepository.findById(orderId)).isNotPresent();
    }

    @Test
    void givenOrderEntity_whenUpdated_thenChangesArePersisted() {
        // given
        orderEntity.setOrderProducts(List.of(new OrderProduct(null, orderEntity, UUID.randomUUID(), 6L)));

        // when
        Order updatedOrder = orderRepository.save(orderEntity);

        // then
        assertThat(updatedOrder.getOrderProducts()).hasSize(1);
        Optional<Order> foundOrder = orderRepository.findById(orderEntity.getId());
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getOrderProducts().stream().anyMatch(product -> product.getQuantity() == 6L)).isTrue();
    }

    @Test
    void givenMultipleOrderEntities_whenFindAll_thenAllAreReturned() {
        // given
        Order anotherOrder = new Order();
        anotherOrder.setTimestamp(LocalDateTime.now());
        orderRepository.save(anotherOrder);

        // when
        var allOrders = orderRepository.findAll();

        // then
        assertThat(allOrders).hasSize(2);
    }


    @Test
    void givenNoOrders_whenFindAll_thenReturnEmptyList() {
        // given
        orderRepository.deleteAll();

        // when
        var allOrders = orderRepository.findAll();

        // then
        assertThat(allOrders).isEmpty();
    }
}