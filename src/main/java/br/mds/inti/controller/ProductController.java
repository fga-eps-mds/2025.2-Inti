package br.mds.inti.controller;

import br.mds.inti.model.dto.ProductSummaryDTO;
import br.mds.inti.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{profileId}/products")
    public ResponseEntity<Page<ProductSummaryDTO>> getProductsByProfile(
            @PathVariable UUID profileId,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            Authentication authentication 
    ) {

        UUID viewerProfileId = null; 
        Page<ProductSummaryDTO> products =
                productService.getProductsByProfile(profileId, viewerProfileId, page, size);

        return ResponseEntity.ok(products);
    }
}
