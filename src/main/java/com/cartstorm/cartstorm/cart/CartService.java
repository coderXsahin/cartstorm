
package com.cartstorm.cartstorm.cart;

import com.cartstorm.cartstorm.product.Product;
import com.cartstorm.cartstorm.product.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(
            CartItemRepository cartItemRepository,
            ProductRepository productRepository
    ) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public CartItem addItem(String customerId, Long productId, int quantity) {
        validateCustomerId(customerId);
        validateQuantity(quantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found"
                ));

        CartItem existingItem = cartItemRepository
                .findByCustomerIdAndProductId(customerId, productId)
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(
                    Math.addExact(existingItem.getQuantity(), quantity)
            );
            return cartItemRepository.save(existingItem);
        }

        CartItem item = new CartItem(customerId, product, quantity);
        return cartItemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public List<CartItem> getCart(String customerId) {
        validateCustomerId(customerId);

        return cartItemRepository
                .findByCustomerIdOrderByCreatedAtAsc(customerId);
    }

    @Transactional
    public CartItem updateQuantity(
            String customerId,
            Long productId,
            int quantity
    ) {
        validateCustomerId(customerId);
        validateQuantity(quantity);

        CartItem item = cartItemRepository
                .findByCustomerIdAndProductId(customerId, productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product is not in this cart"
                ));

        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    @Transactional
    public void removeItem(String customerId, Long productId) {
        validateCustomerId(customerId);

        CartItem item = cartItemRepository
                .findByCustomerIdAndProductId(customerId, productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product is not in this cart"
                ));

        cartItemRepository.delete(item);
    }

    @Transactional
    public void clearCart(String customerId) {
        validateCustomerId(customerId);
        cartItemRepository.deleteByCustomerId(customerId);
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

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be greater than zero"
            );
        }
    }
}
