package br.mds.inti.model.dto;

import br.mds.inti.model.entity.ArtistProducts;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class ProductResponseDTO {

    private UUID id;
    private UUID profileId;
    private String profileUsername;
    private String profileName;
    private String profilePictureUrl;
    private String title;
    private String description;
    private BigDecimal price;
    private String imgLink;
    private Instant createdAt;

    public static ProductResponseDTO fromEntity(ArtistProducts product, String imgLink) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setProfileId(product.getProfile().getId());
        dto.setProfileUsername(product.getProfile().getUsername());
        dto.setProfileName(product.getProfile().getName());
        dto.setProfilePictureUrl(product.getProfile().getProfilePictureUrl());
        dto.setTitle(product.getTitle());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setImgLink(imgLink);
        dto.setCreatedAt(product.getCreatedAt());
        return dto;
    }
}