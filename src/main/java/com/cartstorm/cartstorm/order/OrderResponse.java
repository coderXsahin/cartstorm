
package com.cartstorm.cartstorm.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String customerId,
        BigDecimal totalAmount,
        OrderStatus status,
        Instant createdAt,
        List<OrderLineResponse> items
) {

    public record OrderLineResponse(
            Long productId,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {
    }
}
