package br.mds.inti.repositories;

import br.mds.inti.model.entity.ArtistProducts;
import br.mds.inti.model.entity.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ArtistProductsRepository extends JpaRepository<ArtistProducts, UUID> {

    Page<ArtistProducts> findByProfileAndDeletedAtIsNullOrderByCreatedAtDesc(
            Profile profile,
            Pageable pageable
    );
}
