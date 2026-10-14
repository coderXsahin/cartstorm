
package com.cartstorm.cartstorm.checkout;

import com.cartstorm.cartstorm.order.CustomerOrder;
import com.cartstorm.cartstorm.order.CustomerOrderRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CheckoutService {

    private final CustomerOrderRepository customerOrderRepository;
    private final CheckoutTransactionService checkoutTransactionService;

    public CheckoutService(
            CustomerOrderRepository customerOrderRepository,
            CheckoutTransactionService checkoutTransactionService
    ) {
        this.customerOrderRepository = customerOrderRepository;
        this.checkoutTransactionService = checkoutTransactionService;
    }

    public CustomerOrder checkout(
            String customerId,
            String idempotencyKey
    ) {
        validateCustomerId(customerId);
        validateIdempotencyKey(idempotencyKey);

        var existingOrder =
                customerOrderRepository.findByCustomerIdAndIdempotencyKey(
                        customerId,
                        idempotencyKey
                );

        if (existingOrder.isPresent()) {
            return existingOrder.get();
        }

        try {
            // This call crosses a Spring service boundary.
            // Its transaction commits or rolls back before control returns.
            return checkoutTransactionService.createOrder(
                    customerId,
                    idempotencyKey
            );
        } catch (DataIntegrityViolationException exception) {
            // A concurrent request may have committed this key first.
            // The failed transaction has already rolled back, so the lookup
            // happens outside that failed transaction.
            return customerOrderRepository
                    .findByCustomerIdAndIdempotencyKey(
                            customerId,
                            idempotencyKey
                    )
                    .orElseThrow(() -> exception);
        }
    }

    private void validateCustomerId(String customerId) {
        if (customerId == null || customerId.isBlank()
                || customerId.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Customer ID must contain between 1 and 100 characters"
            );
        }
    }

    private void validateIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()
                || idempotencyKey.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Idempotency-Key must contain between 1 and 100 characters"
            );
        }
    }
}
