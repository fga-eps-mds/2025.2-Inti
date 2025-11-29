package br.mds.inti.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "img_link", nullable = false, length = 512)
    private String imgLink;

    @Column(name = "contact_info", length = 255)
    private String contactInfo;

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags; // Armazenado como String separada por vírgulas ou JSON

    @Column(name = "visibility", nullable = false, length = 50)
    private String visibility; // Ex: PUBLIC, PRIVATE

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
