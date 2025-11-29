package br.mds.inti.service;

import br.mds.inti.model.dto.CreateProductDTO;
import br.mds.inti.model.dto.ProductResponseDTO;
import br.mds.inti.model.entity.Product;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProfileService profileService;

    public ProductResponseDTO createProduct(CreateProductDTO dto, UUID profileId) {
        Profile profile = profileService.getProfileById(profileId);

        Product product = new Product();
        product.setProfile(profile);
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setImgLink(dto.getImgLink());
        product.setContactInfo(dto.getContactInfo());
        product.setVisibility(dto.getVisibility());
        product.setTags(dto.getTags().stream().collect(Collectors.joining(",")));

        // Validação de preço
        if (product.getPrice() != null && product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O preço não pode ser negativo.");
        }

        // Validação de URL simples (pode ser melhorada)
        if (product.getImgLink() != null && !product.getImgLink().matches("^(http|https)://.*")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O link da imagem deve ser uma URL válida.");
        }

        product = productRepository.save(product);
        return ProductResponseDTO.fromEntity(product);
    }

    public Page<ProductResponseDTO> getPublicProducts(String tag, String text, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productRepository.findPublicProductsWithFilters(tag, text, minPrice, maxPrice, pageable)
                .map(ProductResponseDTO::fromEntity);
    }

    public ProductResponseDTO getProductById(UUID id) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anúncio não encontrado."));
        return ProductResponseDTO.fromEntity(product);
    }

    public ProductResponseDTO updateProduct(UUID id, CreateProductDTO dto, UUID profileId) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anúncio não encontrado."));

        if (!product.getProfile().getId().equals(profileId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para editar este anúncio.");
        }

        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setImgLink(dto.getImgLink());
        product.setContactInfo(dto.getContactInfo());
        product.setVisibility(dto.getVisibility());
        product.setTags(dto.getTags().stream().collect(Collectors.joining(",")));

        // Validação de preço
        if (product.getPrice() != null && product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O preço não pode ser negativo.");
        }

        // Validação de URL simples (pode ser melhorada)
        if (product.getImgLink() != null && !product.getImgLink().matches("^(http|https)://.*")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O link da imagem deve ser uma URL válida.");
        }

        product = productRepository.save(product);
        return ProductResponseDTO.fromEntity(product);
    }

    public void deleteProduct(UUID id, UUID profileId) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anúncio não encontrado."));

        if (!product.getProfile().getId().equals(profileId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para remover este anúncio.");
        }

        product.setDeletedAt(Instant.now());
        productRepository.save(product);
    }
}
