
package com.cartstorm.cartstorm.inventory;

import com.cartstorm.cartstorm.product.Product;
import com.cartstorm.cartstorm.product.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InventoryService {

    private final ProductRepository productRepository;

    public InventoryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public Product reserveStock(Long productId, int quantity) {
        if (quantity <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be greater than zero"
            );
        }

        int updatedRows =
                productRepository.reserveStock(productId, quantity);

        if (updatedRows == 0) {
            boolean productExists =
                    productRepository.existsById(productId);

            if (!productExists) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found"
                );
            }

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Insufficient stock"
            );
        }

        return productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found"
                ));
    }
}
