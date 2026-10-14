
package com.cartstorm.cartstorm.inventory;

import com.cartstorm.cartstorm.product.Product;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@Validated
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/{productId}/reserve")
    public Product reserveStock(
            @PathVariable Long productId,
            @RequestParam @Min(value = 1, message = "Quantity must be positive")
            int quantity) {

        return inventoryService.reserveStock(productId, quantity);
    }
}
