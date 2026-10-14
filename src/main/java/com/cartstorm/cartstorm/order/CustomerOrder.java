
package com.cartstorm.cartstorm.order;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
    name = "customer_orders",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_order_customer_idempotency",
            columnNames = {"customer_id", "idempotency_key"}
        )
    },
    indexes = {
        @Index(
            name = "idx_orders_customer_created",
            columnList = "customer_id, created_at"
        )
    }
)
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false, length = 100)
    private String customerId;

    @Column(
        name = "idempotency_key",
        nullable = false,
        length = 100
    )
    private String idempotencyKey;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected CustomerOrder() {
    }

    public CustomerOrder(
            String customerId,
            BigDecimal totalAmount,
            String idempotencyKey
    ) {
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.idempotencyKey = idempotencyKey;
        this.status = OrderStatus.CONFIRMED;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
