
package com.cartstorm.cartstorm.checkout;

import com.cartstorm.cartstorm.order.CustomerOrder;
import com.cartstorm.cartstorm.order.CustomerOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CheckoutServiceTest {

    private CustomerOrderRepository customerOrderRepository;
    private CheckoutTransactionService checkoutTransactionService;
    private CheckoutService checkoutService;

    @BeforeEach
    void setUp() {
        customerOrderRepository = mock(CustomerOrderRepository.class);
        checkoutTransactionService =
                mock(CheckoutTransactionService.class);

        checkoutService = new CheckoutService(
                customerOrderRepository,
                checkoutTransactionService
        );
    }

    @Test
    void shouldReturnExistingOrderForRepeatedIdempotencyKey() {
        CustomerOrder existingOrder = new CustomerOrder(
                "customer-101",
                new BigDecimal("799.00"),
                "checkout-key-001"
        );

        when(customerOrderRepository
                .findByCustomerIdAndIdempotencyKey(
                        "customer-101",
                        "checkout-key-001"
                ))
                .thenReturn(Optional.of(existingOrder));

        CustomerOrder result = checkoutService.checkout(
                "customer-101",
                "checkout-key-001"
        );

        assertThat(result).isSameAs(existingOrder);

        verify(checkoutTransactionService, never())
                .createOrder(anyString(), anyString());
    }

    @Test
    void shouldCreateOrderWhenIdempotencyKeyIsNew() {
        CustomerOrder newOrder = new CustomerOrder(
                "customer-101",
                new BigDecimal("799.00"),
                "checkout-key-002"
        );

        when(customerOrderRepository
                .findByCustomerIdAndIdempotencyKey(
                        "customer-101",
                        "checkout-key-002"
                ))
                .thenReturn(Optional.empty());

        when(checkoutTransactionService.createOrder(
                "customer-101",
                "checkout-key-002"
        )).thenReturn(newOrder);

        CustomerOrder result = checkoutService.checkout(
                "customer-101",
                "checkout-key-002"
        );

        assertThat(result).isSameAs(newOrder);

        verify(checkoutTransactionService)
                .createOrder("customer-101", "checkout-key-002");
    }

    @Test
    void shouldRejectBlankCustomerId() {
        assertThatThrownBy(() ->
                checkoutService.checkout(" ", "checkout-key-003")
        )
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400 BAD_REQUEST");

        verifyNoInteractions(customerOrderRepository);
        verifyNoInteractions(checkoutTransactionService);
    }

    @Test
    void shouldRejectBlankIdempotencyKey() {
        assertThatThrownBy(() ->
                checkoutService.checkout("customer-101", " ")
        )
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400 BAD_REQUEST");

        verifyNoInteractions(customerOrderRepository);
        verifyNoInteractions(checkoutTransactionService);
    }
}
