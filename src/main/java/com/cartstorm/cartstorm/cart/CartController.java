
package com.cartstorm.cartstorm.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@Validated
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/items")
    public CartItem addItem(
            @RequestParam
            @NotBlank
            @Size(max = 100)
            String customerId,

            @RequestParam Long productId,

            @RequestParam
            @Min(value = 1, message = "Quantity must be positive")
            int quantity
    ) {
        return cartService.addItem(customerId, productId, quantity);
    }

    @GetMapping
    public List<CartItem> getCart(
            @RequestParam
            @NotBlank
            @Size(max = 100)
            String customerId
    ) {
        return cartService.getCart(customerId);
    }

    @PutMapping("/items/{productId}")
    public CartItem updateQuantity(
            @RequestParam
            @NotBlank
            @Size(max = 100)
            String customerId,

            @PathVariable Long productId,

            @RequestParam
            @Min(value = 1, message = "Quantity must be positive")
            int quantity
    ) {
        return cartService.updateQuantity(customerId, productId, quantity);
    }

    @DeleteMapping("/items/{productId}")
    public void removeItem(
            @RequestParam
            @NotBlank
            @Size(max = 100)
            String customerId,

            @PathVariable Long productId
    ) {
        cartService.removeItem(customerId, productId);
    }

    @DeleteMapping
    public void clearCart(
            @RequestParam
            @NotBlank
            @Size(max = 100)
            String customerId
    ) {
        cartService.clearCart(customerId);
    }
}
