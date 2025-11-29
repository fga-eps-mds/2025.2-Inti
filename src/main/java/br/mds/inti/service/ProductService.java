package br.mds.inti.service;

import br.mds.inti.model.dto.ProductSummaryDTO;
import br.mds.inti.model.entity.ArtistProducts;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.repositories.ArtistProductsRepository;
import br.mds.inti.repositories.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ArtistProductsRepository artistProductsRepository;
    private final ProfileRepository profileRepository;
   
    public Page<ProductSummaryDTO> getProductsByProfile(UUID profileId,
                                                        UUID viewerProfileId,
                                                        int page,
                                                        int size) {

        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

       

        Pageable pageable = PageRequest.of(page, size);

        Page<ArtistProducts> productsPage =
                artistProductsRepository.findByProfileAndDeletedAtIsNullOrderByCreatedAtDesc(profile, pageable);

        return productsPage.map(this::toProductSummaryDTO);
    }

    
    public Page<ProductSummaryDTO> getProductsByProfileUsername(String username, int page, int size) {
        Profile profile = profileRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        Pageable pageable = PageRequest.of(page, size);

        Page<ArtistProducts> productsPage =
                artistProductsRepository.findByProfileAndDeletedAtIsNullOrderByCreatedAtDesc(profile, pageable);

        return productsPage.map(this::toProductSummaryDTO);
    }


    private ProductSummaryDTO toProductSummaryDTO(ArtistProducts product) {
        return new ProductSummaryDTO(
                product.getId(),
                product.getTitle(),
                buildImageLink(product.getBlobName()),
                bigDecimalToDoubleOrNull(product.getPrice()),
                product.getDescription()
        );
    }

    private Double bigDecimalToDoubleOrNull(BigDecimal value) {
        return value != null ? value.doubleValue() : null;
    }

    private String buildImageLink(String blobName) {
        if (blobName == null || blobName.isBlank()) {
            return null;
        }
        return blobName;
    }
}
