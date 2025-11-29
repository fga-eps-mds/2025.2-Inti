package br.mds.inti.controller;

import br.mds.inti.model.dto.CreateProductDTO;
import br.mds.inti.model.dto.ProductResponseDTO;
import br.mds.inti.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody CreateProductDTO dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID profileId = UUID.fromString(authentication.getName()); // Assumindo que o nome da autenticação é o profileId

        ProductResponseDTO newProduct = productService.createProduct(dto, profileId);
        return ResponseEntity.status(HttpStatus.CREATED).body(newProduct);
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> getProducts(
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String text,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<ProductResponseDTO> products = productService.getPublicProducts(tag, text, minPrice, maxPrice, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable UUID id) {
        ProductResponseDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable UUID id, @Valid @RequestBody CreateProductDTO dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID profileId = UUID.fromString(authentication.getName());

        ProductResponseDTO updatedProduct = productService.updateProduct(id, dto, profileId);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID profileId = UUID.fromString(authentication.getName());

        productService.deleteProduct(id, profileId);
        return ResponseEntity.noContent().build();
    }
}
