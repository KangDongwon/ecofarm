package com.ecofarm.demo.controller;

import com.ecofarm.demo.auth.dto.ErrorResponse;
import com.ecofarm.demo.dto.ProductDTO;
import com.ecofarm.demo.entity.Product;
import com.ecofarm.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    @GetMapping
    public List<ProductDTO> getAllProducts() {
        return productService.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        Optional<Product> product = productService.findById(id);
        return product.map(p -> ResponseEntity.ok(convertToDTO(p)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody ProductDTO productDTO) {

        if (productService.existsByProductName(productDTO.getProductName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Product already in use."));
        }

        Product product = convertToEntity(productDTO);
        Product savedProduct = productService.save(product);
        return ResponseEntity.ok(convertToDTO(savedProduct));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update")
    public ResponseEntity<Product> updateProduct(@RequestBody ProductDTO productDTO) {
        try {
            Product updatedProduct = productService.updateProduct(productDTO);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private ProductDTO convertToDTO(Product product) {
        return ProductDTO.builder()
                .productID(product.getProductID())
                .productName(product.getProductName())
                .description(product.getDescription())
                .build();
    }

    private Product convertToEntity(ProductDTO dto) {
        Product product = new Product();
        product.setProductID(dto.getProductID());
        product.setProductName(dto.getProductName());
        product.setDescription(dto.getDescription());
        return product;
    }
}
