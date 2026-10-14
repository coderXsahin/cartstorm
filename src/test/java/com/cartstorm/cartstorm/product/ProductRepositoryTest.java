
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
        // Arrange
        Product product = new Product();
        product.setName("Test Keyboard");
        product.setDescription("Mechanical keyboard");
        product.setPrice(new BigDecimal("1499.00"));
        product.setStock(10);
        product.setCategory("Electronics");

        // Act
        Product savedProduct = productRepository.saveAndFlush(product);
        var foundProduct = productRepository.findById(savedProduct.getId());

        // Assert
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getName()).isEqualTo("Test Keyboard");
        assertThat(foundProduct.get().getPrice())
                .isEqualByComparingTo(new BigDecimal("1499.00"));
        assertThat(foundProduct.get().getStock()).isEqualTo(10);
    }
}
