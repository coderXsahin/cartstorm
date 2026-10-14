
package com.cartstorm.cartstorm.inventory;

import com.cartstorm.cartstorm.product.Product;
import com.cartstorm.cartstorm.product.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class InventoryServiceTest {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private EntityManager entityManager;
    
@Test
void shouldReduceStockWhenReservationIsSuccessful() {
    Product product = createProductWithStock(10);

    // Flush the insert, then clear cached entities so the
    // database update is not hidden by a stale JPA entity.
    entityManager.flush();
    Long productId = product.getId();
    entityManager.clear();

    InventoryService inventoryService =
            new InventoryService(productRepository);

    Product updated = inventoryService.reserveStock(productId, 3);

    // Reload from the database to verify the actual stock.
    entityManager.clear();
    Product reloaded = productRepository.findById(productId)
            .orElseThrow();

    assertThat(updated.getStock()).isEqualTo(7);
    assertThat(reloaded.getStock()).isEqualTo(7);
}


    @Test
    void shouldNotReduceStockWhenQuantityExceedsAvailableStock() {
        Product product = createProductWithStock(5);
        InventoryService inventoryService =
                new InventoryService(productRepository);

        assertThatThrownBy(() ->
                inventoryService.reserveStock(product.getId(), 6))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(
                        ((ResponseStatusException) exception)
                                .getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));

        assertThat(productRepository.findById(product.getId())
                .orElseThrow().getStock()).isEqualTo(5);
    }

    @Test
    void shouldRejectZeroQuantity() {
        Product product = createProductWithStock(5);
        InventoryService inventoryService =
                new InventoryService(productRepository);

        assertThatThrownBy(() ->
                inventoryService.reserveStock(product.getId(), 0))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(
                        ((ResponseStatusException) exception)
                                .getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));

        assertThat(productRepository.findById(product.getId())
                .orElseThrow().getStock()).isEqualTo(5);
    }

    private Product createProductWithStock(int stock) {
        Product product = new Product();
        product.setName("Inventory Test Product");
        product.setDescription("Product for inventory tests");
        product.setPrice(new BigDecimal("499.00"));
        product.setStock(stock);
        product.setCategory("Testing");

        return productRepository.saveAndFlush(product);
    }
}
