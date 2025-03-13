package com.example.inventoryservice;

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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@SpringJUnitConfig
class InventoryRepositoryTest {

    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15.4-alpine")
            .withDatabaseName("inventory_db")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private InventoryRepository inventoryRepository;

    private InventoryEntity savedEntity;

    @BeforeEach
    void setUp() {
        InventoryEntity entity = new InventoryEntity();
        entity.setProductId(UUID.randomUUID());
        entity.setStock(100L);
        entity.setMinimumQuantity(10L);
        entity.setMaximumQuantity(500L);
        savedEntity = inventoryRepository.save(entity);
    }

    @AfterEach
    void tearDown() {
        inventoryRepository.deleteAll();
    }

    @DynamicPropertySource
    static void properties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @Test
    void givenInventoryEntity_whenSaved_thenItExistsInDatabase() {
        // then
        assertThat(savedEntity).isNotNull();
        assertThat(savedEntity.getInventoryId()).isNotNull();
        assertThat(inventoryRepository.findAll()).hasSize(1);
    }

    @Test
    void givenProductId_whenFindByProductId_thenReturnInventoryEntity() {
        // when
        Optional<InventoryEntity> foundEntity = inventoryRepository.findByProductId(savedEntity.getProductId());

        // then
        assertThat(foundEntity).isPresent();
        assertThat(foundEntity.get().getProductId()).isEqualTo(savedEntity.getProductId());
        assertThat(foundEntity.get().getStock()).isEqualTo(savedEntity.getStock());
    }

    @Test
    void givenNonExistentProductId_whenFindByProductId_thenReturnEmpty() {
        // when
        Optional<InventoryEntity> foundEntity = inventoryRepository.findByProductId(UUID.randomUUID());

        // then
        assertThat(foundEntity).isNotPresent();
    }

    @Test
    void givenExistingInventory_whenDecrementStock_thenStockIsUpdated() {
        // given
        UUID inventoryId = savedEntity.getInventoryId();
        Long decrementAmount = 50L;

        // when
        int updatedRows = inventoryRepository.decrementStock(inventoryId, decrementAmount);

        // then
        assertThat(updatedRows).isEqualTo(1);
        Optional<InventoryEntity> updatedEntity = inventoryRepository.findById(inventoryId);
        assertThat(updatedEntity).isPresent();
        assertThat(updatedEntity.get().getStock()).isEqualTo(50L);
    }

    @Test
    void givenLowStock_whenDecrementStock_thenNoUpdateOccurs() {
        // given
        savedEntity.setStock(30L);
        inventoryRepository.save(savedEntity);

        // when
        int updatedRows = inventoryRepository.decrementStock(savedEntity.getProductId(), 50L);

        // then
        assertThat(updatedRows).isEqualTo(0);
        Optional<InventoryEntity> unchangedEntity = inventoryRepository.findByProductId(savedEntity.getProductId());
        assertThat(unchangedEntity).isPresent();
        assertThat(unchangedEntity.get().getStock()).isEqualTo(30L);
    }

    @Test
    void givenInventoryEntity_whenDeleted_thenEntityDoesNotExist() {
        // given
        UUID inventoryId = savedEntity.getInventoryId();

        // when
        inventoryRepository.deleteById(inventoryId);

        // then
        assertThat(inventoryRepository.findById(inventoryId)).isNotPresent();
    }

    @Test
    void givenInventoryEntity_whenUpdated_thenChangesArePersisted() {
        // given
        savedEntity.setStock(200L);

        // when
        InventoryEntity updatedEntity = inventoryRepository.save(savedEntity);

        // then
        assertThat(updatedEntity.getStock()).isEqualTo(200L);
        Optional<InventoryEntity> foundEntity = inventoryRepository.findByProductId(savedEntity.getProductId());
        assertThat(foundEntity).isPresent();
        assertThat(foundEntity.get().getStock()).isEqualTo(200L);
    }

    @Test
    void givenMultipleEntities_whenFindAll_thenAllAreReturned() {
        // given
        InventoryEntity anotherEntity = new InventoryEntity();
        anotherEntity.setProductId(UUID.randomUUID());
        anotherEntity.setStock(200L);
        anotherEntity.setMinimumQuantity(20L);
        anotherEntity.setMaximumQuantity(400L);
        inventoryRepository.save(anotherEntity);

        // when
        var allEntities = inventoryRepository.findAll();

        // then
        assertThat(allEntities).hasSize(2);
        assertThat(allEntities.stream().map(InventoryEntity::getStock)).contains(100L, 200L);
    }
}