package com.cartstorm.cartstorm.product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest {
    @Autowired
    private ProductRepository productRepository;
    @Test
    void shouldSaveAndFindProductById() {
        Product product = createProduct("Test Keyboard", 10);
        Product savedProduct = productRepository.saveAndFlush(product);
        var foundProduct = productRepository.findById(savedProduct.getId());
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getName()).isEqualTo("Test Keyboard");
        assertThat(foundProduct.get().getPrice())
                .isEqualByComparingTo(new BigDecimal("1499.00"));
        assertThat(foundProduct.get().getStock()).isEqualTo(10);
    }
    @Test
    void shouldReserveStockWhenEnoughStockIsAvailable() {
        Product product = productRepository.saveAndFlush(
                createProduct("Reservation Test Product", 10)
        );
        int updatedRows = productRepository.reserveStock(product.getId(), 3);
        assertThat(updatedRows).isEqualTo(1);
        assertThat(productRepository.findById(product.getId())
                .orElseThrow().getStock()).isEqualTo(7);
    }
    @Test
    void shouldNotReserveStockWhenQuantityExceedsAvailableStock() {
        Product product = productRepository.saveAndFlush(
                createProduct("Insufficient Stock Test Product", 5)
        );
        int updatedRows = productRepository.reserveStock(product.getId(), 6);
        assertThat(updatedRows).isZero();
        assertThat(productRepository.findById(product.getId())
                .orElseThrow().getStock()).isEqualTo(5);
    }
    private Product createProduct(String name, int stock) {
        Product product = new Product();
        product.setName(name);
        product.setDescription("Repository regression test product");
        product.setPrice(new BigDecimal("1499.00"));
        product.setStock(stock);
        product.setCategory("Test");
        return product;
    }
}
