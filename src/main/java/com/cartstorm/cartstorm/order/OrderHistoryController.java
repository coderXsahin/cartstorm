
package com.cartstorm.cartstorm.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Validated
public class OrderHistoryController {

    private final OrderHistoryService orderHistoryService;

    public OrderHistoryController(OrderHistoryService orderHistoryService) {
        this.orderHistoryService = orderHistoryService;
    }

    @GetMapping
    public List<OrderResponse> getOrderHistory(
            @RequestParam
            @NotBlank
            @Size(max = 100)
            String customerId
    ) {
        return orderHistoryService.getOrderHistory(customerId);
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(
            @RequestParam
            @NotBlank
            @Size(max = 100)
            String customerId,
            @PathVariable Long orderId
    ) {
        return orderHistoryService.getOrder(customerId, orderId);
    }
}
