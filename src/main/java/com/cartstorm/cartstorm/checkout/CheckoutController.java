
package com.cartstorm.cartstorm.checkout;

import com.cartstorm.cartstorm.order.CustomerOrder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkout")
@Validated
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping
    public CustomerOrder checkout(
            @RequestParam
            @NotBlank
            @Size(max = 100)
            String customerId,

            @RequestHeader("Idempotency-Key")
            @NotBlank
            @Size(max = 100)
            String idempotencyKey
    ) {
        return checkoutService.checkout(customerId, idempotencyKey);
    }
}
