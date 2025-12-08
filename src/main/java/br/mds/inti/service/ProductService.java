package br.mds.inti.service;

import br.mds.inti.model.dto.CreateProductDTO;
import br.mds.inti.model.dto.EditProductDTO;
import br.mds.inti.model.dto.ProductResponseDTO;
import br.mds.inti.model.dto.ProductSummaryDTO;
import br.mds.inti.model.entity.ArtistProducts;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private BlobService blobService;

    @Autowired
    private PostService postService;

    public ProductResponseDTO createProduct(CreateProductDTO dto, UUID profileId) {
        Profile profile = profileService.getProfileById(profileId);

        ArtistProducts product = new ArtistProducts();
        product.setProfile(profile);
        product.setTitle(dto.title());
        product.setDescription(dto.description());
        product.setPrice(dto.price());
        String blobName = storeProductImage(profileId, dto.image(), true);
        product.setBlobName(blobName);
        product.setCreatedAt(Instant.now());

        if (product.getPrice() != null && product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O preço não pode ser negativo.");
        }

        product = productRepository.save(product);
        return toProductResponse(product);
    }

    public Page<ProductResponseDTO> getPublicProducts(Pageable pageable) {
        return productRepository.findByDeletedAtIsNull(pageable)
                .map(this::toProductResponse);
    }

    public ProductResponseDTO getProductById(UUID id) {
        ArtistProducts product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anúncio não encontrado."));
        return toProductResponse(product);
    }

    public List<ProductResponseDTO> getProductsByProfile(UUID profileId) {
        profileService.getProfileById(profileId);

        return productRepository.findByProfileIdAndDeletedAtIsNull(profileId).stream()
                .map(this::toProductResponse)
                .collect(Collectors.toList());
    }

    public ProductResponseDTO updateProduct(UUID id, EditProductDTO dto, UUID profileId) {
        ArtistProducts product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anúncio não encontrado."));

        if (!product.getProfile().getId().equals(profileId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para editar este anúncio.");
        }

        if (dto.title() != null) {
            product.setTitle(dto.title());
        }

        if (dto.description() != null) {
            product.setDescription(dto.description());
        }

        if (dto.price() != null) {
            product.setPrice(dto.price());
        }

        if (dto.image() != null) {
            String newBlobName = storeProductImage(profileId, dto.image(), false);
            if (newBlobName != null) {
                product.setBlobName(newBlobName);
            }
        }

        if (product.getPrice() != null && product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O preço não pode ser negativo.");
        }

        product = productRepository.save(product);
        return toProductResponse(product);
    }

    public void deleteProduct(UUID id, UUID profileId) {
        ArtistProducts product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anúncio não encontrado."));

        if (!product.getProfile().getId().equals(profileId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Você não tem permissão para remover este anúncio.");
        }

        product.setDeletedAt(Instant.now());
        productRepository.save(product);
    }

    private String storeProductImage(UUID profileId, MultipartFile image, boolean required) {
        if (image == null || image.isEmpty()) {
            if (required) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A imagem do produto é obrigatória.");
            }
            return null;
        }

        try {
            return blobService.uploadImage(profileId, image);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao processar a imagem do produto.", e);
        }
    }

    private ProductResponseDTO toProductResponse(ArtistProducts product) {
        String imgLink = postService.generateImageUrl(product.getBlobName());
        return ProductResponseDTO.fromEntity(product, imgLink);
    }

    public Page<ProductSummaryDTO> getProfileProducts(UUID profileId, Pageable pageable) {
        profileService.getProfileById(profileId);

        return productRepository.findByProfileIdAndDeletedAtIsNull(profileId, pageable)
                .map(this::toProductSummaryDTO);
    }

    private ProductSummaryDTO toProductSummaryDTO(ArtistProducts product) {
        String imgLink = postService.generateImageUrl(product.getBlobName());
        String shortDescription = truncateDescription(product.getDescription());

        return new ProductSummaryDTO(
                product.getId(),
                product.getTitle(),
                imgLink,
                product.getPrice(),
                shortDescription
        );
    }

    private String truncateDescription(String description) {
        if (description == null || description.isBlank()) {
            return "";
        }

        final int MAX_LENGTH = 100;
        if (description.length() <= MAX_LENGTH) {
            return description;
        }

        return description.substring(0, MAX_LENGTH) + "...";
    }

}
