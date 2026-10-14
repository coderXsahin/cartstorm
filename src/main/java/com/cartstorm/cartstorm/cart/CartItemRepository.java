
package com.cartstorm.cartstorm.cart;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @EntityGraph(attributePaths = {"product"})
    List<CartItem> findByCustomerIdOrderByCreatedAtAsc(String customerId);

    Optional<CartItem> findByCustomerIdAndProductId(
            String customerId,
            Long productId
    );

    void deleteByCustomerIdAndProductId(
            String customerId,
            Long productId
    );

    void deleteByCustomerId(String customerId);
}
