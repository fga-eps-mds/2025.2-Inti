package br.mds.inti.model.dto;

import br.mds.inti.model.entity.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class ProductResponseDTO {

    private UUID id;
    private UUID profileId;
    private String title;
    private String description;
    private BigDecimal price;
    private String imgLink;
    private String contactInfo;
    private List<String> tags;
    private String visibility;
    private Instant createdAt;

    public static ProductResponseDTO fromEntity(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setProfileId(product.getProfile().getId());
        dto.setTitle(product.getTitle());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setImgLink(product.getImgLink());
        dto.setContactInfo(product.getContactInfo());
        dto.setVisibility(product.getVisibility());
        dto.setCreatedAt(product.getCreatedAt());

        if (product.getTags() != null && !product.getTags().isEmpty()) {
            dto.setTags(Arrays.stream(product.getTags().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList()));
        } else {
            dto.setTags(List.of());
        }

        return dto;
    }
}
    