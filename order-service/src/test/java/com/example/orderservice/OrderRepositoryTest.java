package com.example.orderservice;

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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@SpringJUnitConfig
class OrderRepositoryTest {

    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("order_db")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private OrderRepository orderRepository;

    private OrderEntity orderEntity;

    @BeforeEach
    void setUp() {
        orderEntity = new OrderEntity();
        orderEntity.setOrderDate(LocalDateTime.now());
        orderEntity.setStatus("PENDING");
        orderEntity.setTotalAmount(100.0);
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
        Optional<OrderEntity> foundOrder = orderRepository.findById(orderEntity.getId());

        // then
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getStatus()).isEqualTo("PENDING");
        assertThat(foundOrder.get().getTotalAmount()).isEqualTo(100.0);
    }

    @Test
    void givenNonExistentOrderId_whenFindById_thenReturnEmpty() {
        // when
        Optional<OrderEntity> foundOrder = orderRepository.findById(UUID.randomUUID());

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
        orderEntity.setStatus("COMPLETED");

        // when
        OrderEntity updatedOrder = orderRepository.save(orderEntity);

        // then
        assertThat(updatedOrder.getStatus()).isEqualTo("COMPLETED");
        Optional<OrderEntity> foundOrder = orderRepository.findById(orderEntity.getId());
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void givenMultipleOrderEntities_whenFindAll_thenAllAreReturned() {
        // given
        OrderEntity anotherOrder = new OrderEntity();
        anotherOrder.setOrderDate(LocalDateTime.now());
        anotherOrder.setStatus("SHIPPED");
        anotherOrder.setTotalAmount(200.0);
        orderRepository.save(anotherOrder);

        // when
        var allOrders = orderRepository.findAll();

        // then
        assertThat(allOrders).hasSize(2);
        assertThat(allOrders.stream().map(OrderEntity::getStatus)).contains("PENDING", "SHIPPED");
    }

    @Test
    void givenOrderEntity_whenSaveWithNullFields_thenThrowException() {
        // given
        OrderEntity invalidOrder = new OrderEntity();
        invalidOrder.setOrderDate(null); // Invalid field
        invalidOrder.setStatus("PENDING");
        invalidOrder.setTotalAmount(50.0);

        // when/then
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> orderRepository.save(invalidOrder));
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