package br.mds.inti.controller;

import br.mds.inti.model.dto.CreateProductDTO;
import br.mds.inti.model.dto.EditProductDTO;
import br.mds.inti.model.dto.ProductResponseDTO;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponseDTO> createProduct(
            @ModelAttribute CreateProductDTO dto) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Profile profile = (Profile) authentication.getPrincipal();

        ProductResponseDTO newProduct = productService.createProduct(dto, profile.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(newProduct);
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> getProducts(
            @PageableDefault(size = 10) Pageable pageable) {

        Page<ProductResponseDTO> products = productService.getPublicProducts(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable UUID id) {
        ProductResponseDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/profile/{profileId}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByProfile(@PathVariable UUID profileId) {
        List<ProductResponseDTO> products = productService.getProductsByProfile(profileId);
        return ResponseEntity.ok(products);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable UUID id,
            @ModelAttribute EditProductDTO dto) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Profile profile = (Profile) authentication.getPrincipal();

        ProductResponseDTO updatedProduct = productService.updateProduct(id, dto, profile.getId());
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Profile profile = (Profile) authentication.getPrincipal();

        productService.deleteProduct(id, profile.getId());
        return ResponseEntity.noContent().build();
    }

}