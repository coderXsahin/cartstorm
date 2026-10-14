
package com.cartstorm.cartstorm.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerOrderRepository
        extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findByCustomerIdOrderByCreatedAtDesc(
            String customerId
    );

    Optional<CustomerOrder> findByIdAndCustomerId(
            Long id,
            String customerId
    );

    Optional<CustomerOrder> findByCustomerIdAndIdempotencyKey(
            String customerId,
            String idempotencyKey
    );
}
