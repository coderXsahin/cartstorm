
package com.cartstorm.cartstorm.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByNameContainingIgnoreCase(
            String name, Pageable pageable);

    Page<Product> findByCategoryIgnoreCase(
            String category, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseAndCategoryIgnoreCase(
            String name, String category, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Product p
        SET p.stock = p.stock - :quantity
        WHERE p.id = :productId
          AND p.stock >= :quantity
        """)
    int reserveStock(
            @Param("productId") Long productId,
            @Param("quantity") int quantity);
}
