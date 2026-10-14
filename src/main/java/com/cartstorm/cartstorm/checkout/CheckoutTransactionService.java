
package com.cartstorm.cartstorm.checkout;

import com.cartstorm.cartstorm.cart.CartItem;
import com.cartstorm.cartstorm.cart.CartItemRepository;
import com.cartstorm.cartstorm.order.CustomerOrder;
import com.cartstorm.cartstorm.order.CustomerOrderRepository;
import com.cartstorm.cartstorm.order.OrderItem;
import com.cartstorm.cartstorm.order.OrderItemRepository;
import com.cartstorm.cartstorm.product.Product;
import com.cartstorm.cartstorm.product.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CheckoutTransactionService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final OrderItemRepository orderItemRepository;

    public CheckoutTransactionService(
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            CustomerOrderRepository customerOrderRepository,
            OrderItemRepository orderItemRepository
    ) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.customerOrderRepository = customerOrderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public CustomerOrder createOrder(
            String customerId,
            String idempotencyKey
    ) {
        List<CartItem> cartItems =
                cartItemRepository.findByCustomerIdOrderByCreatedAtAsc(
                        customerId
                );

        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot checkout an empty cart"
            );
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem item : cartItems) {
            Product product = item.getProduct();

            if (product.getPrice() == null || item.getQuantity() <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cart contains invalid item data"
                );
            }

            totalAmount = totalAmount.add(
                    product.getPrice().multiply(
                            BigDecimal.valueOf(item.getQuantity())
                    )
            );
        }

        // The unique database constraint claims this customer/key pair.
        // A competing request with the same key cannot create another order.
        CustomerOrder order = customerOrderRepository.saveAndFlush(
                new CustomerOrder(
                        customerId,
                        totalAmount,
                        idempotencyKey
                )
        );

        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem item : cartItems) {
            Product product = item.getProduct();

            int updatedRows = productRepository.reserveStock(
                    product.getId(),
                    item.getQuantity()
            );

            if (updatedRows == 0) {
                boolean productExists =
                        productRepository.existsById(product.getId());

                if (!productExists) {
                    throw new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "A product in the cart no longer exists"
                    );
                }

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Insufficient stock for product: "
                                + product.getName()
                );
            }

            orderItems.add(
                    new OrderItem(
                            order,
                            product.getId(),
                            product.getName(),
                            item.getQuantity(),
                            product.getPrice()
                    )
            );
        }

        orderItemRepository.saveAll(orderItems);
        cartItemRepository.deleteByCustomerId(customerId);

        return order;
    }
}
