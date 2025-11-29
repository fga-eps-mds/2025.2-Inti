package br.mds.inti.repositories;

import br.mds.inti.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByIdAndDeletedAtIsNull(UUID id);

    Page<Product> findByDeletedAtIsNull(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL AND p.visibility = 'PUBLIC' AND " +
            "(:tag IS NULL OR p.tags LIKE %:tag%) AND " +
            "(:text IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :text, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :text, '%'))) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> findPublicProductsWithFilters(
            @Param("tag") String tag,
            @Param("text") String text,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    Page<Product> findByProfileIdAndDeletedAtIsNull(UUID profileId, Pageable pageable);
}
