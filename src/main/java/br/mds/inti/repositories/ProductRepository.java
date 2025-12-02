package br.mds.inti.repositories;

import br.mds.inti.model.entity.ArtistProducts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;

public interface ProductRepository extends JpaRepository<ArtistProducts, UUID> {

    Optional<ArtistProducts> findByIdAndDeletedAtIsNull(UUID id);

    Page<ArtistProducts> findByDeletedAtIsNull(Pageable pageable);

    @Query("""
            SELECT p FROM ArtistProducts p
            WHERE p.deletedAt IS NULL
              AND (:tag IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :tag, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :tag, '%')))
              AND (:text IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :text, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :text, '%')))
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
            """)
    Page<ArtistProducts> findPublicProductsWithFilters(
            @Param("tag") String tag,
            @Param("text") String text,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    Page<ArtistProducts> findByProfileIdAndDeletedAtIsNull(UUID profileId, Pageable pageable);
}
