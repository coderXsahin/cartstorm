
package com.cartstorm.cartstorm.order;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class OrderHistoryService {

    private final CustomerOrderRepository customerOrderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderHistoryService(
            CustomerOrderRepository customerOrderRepository,
            OrderItemRepository orderItemRepository
    ) {
        this.customerOrderRepository = customerOrderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrderHistory(String customerId) {
        validateCustomerId(customerId);

        return customerOrderRepository
                .findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(String customerId, Long orderId) {
        validateCustomerId(customerId);

        CustomerOrder order = customerOrderRepository
                .findByIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found"
                ));

        return toResponse(order);
    }

    private OrderResponse toResponse(CustomerOrder order) {
        List<OrderResponse.OrderLineResponse> items =
                orderItemRepository
                        .findByCustomerOrderId(order.getId())
                        .stream()
                        .map(item -> new OrderResponse.OrderLineResponse(
                                item.getProductId(),
                                item.getProductName(),
                                item.getQuantity(),
                                item.getUnitPrice(),
                                item.getLineTotal()
                        ))
                        .toList();

        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                items
        );
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
}
