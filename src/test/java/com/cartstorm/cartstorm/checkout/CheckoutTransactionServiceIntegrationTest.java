
package com.cartstorm.cartstorm.checkout;

import com.cartstorm.cartstorm.cart.CartItem;
import com.cartstorm.cartstorm.cart.CartItemRepository;
import com.cartstorm.cartstorm.order.CustomerOrder;
import com.cartstorm.cartstorm.order.CustomerOrderRepository;
import com.cartstorm.cartstorm.order.OrderItemRepository;
import com.cartstorm.cartstorm.product.Product;
import com.cartstorm.cartstorm.product.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class CheckoutTransactionServiceIntegrationTest {

    @Autowired
    private CheckoutTransactionService checkoutTransactionService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Test
    void shouldRollbackOrderAndStockWhenLaterProductHasInsufficientStock() {
        String uniqueId = UUID.randomUUID().toString();
        String customerId = "rollback-" + uniqueId;
        String idempotencyKey = "key-" + uniqueId;

        Product firstProduct = productRepository.saveAndFlush(
                createProduct("Rollback First " + uniqueId, 10)
        );
        Product secondProduct = productRepository.saveAndFlush(
                createProduct("Rollback Second " + uniqueId, 1)
        );

        Long firstProductId = firstProduct.getId();
        Long secondProductId = secondProduct.getId();

        cartItemRepository.saveAndFlush(
                new CartItem(customerId, firstProduct, 2)
        );
        cartItemRepository.saveAndFlush(
                new CartItem(customerId, secondProduct, 5)
        );

        assertThatThrownBy(() ->
                checkoutTransactionService.createOrder(
                        customerId, idempotencyKey
                )
        )
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException ex =
                            (ResponseStatusException) exception;
                    assertThat(ex.getStatusCode())
                            .isEqualTo(HttpStatus.CONFLICT);
                });

        // No order should remain for this checkout.
        assertThat(customerOrderRepository
                .findByCustomerIdAndIdempotencyKey(
                        customerId, idempotencyKey
                )).isEmpty();

        // Both products should retain their original stock.
        assertThat(productRepository.findById(firstProductId)
                .orElseThrow().getStock()).isEqualTo(10);
        assertThat(productRepository.findById(secondProductId)
                .orElseThrow().getStock()).isEqualTo(1);

        // Both cart items should remain for this customer.
        assertThat(cartItemRepository
                .findByCustomerIdOrderByCreatedAtAsc(customerId))
                .extracting(item -> item.getProduct().getId())
                .containsExactlyInAnyOrder(
                        firstProductId, secondProductId
                );

        // No order exists for this customer/key, so no order items
        // can belong to that checkout.
        assertThat(customerOrderRepository
                .findByCustomerIdAndIdempotencyKey(
                        customerId, idempotencyKey
                ))
                .isEmpty();
    }

    private Product createProduct(String name, int stock) {
        Product product = new Product();
        product.setName(name);
        product.setDescription("Checkout rollback integration test");
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(stock);
        product.setCategory("Integration Test");
        return product;
    }
}
