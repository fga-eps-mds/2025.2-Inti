package br.mds.inti.repositories;

import br.mds.inti.model.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {

    Optional<Post> findById(UUID id);

    @Modifying
    @Query("update Post p set p.deletedAt = CURRENT_TIMESTAMP where p.id = ?1")
    void softDeletePost(@Param(value = "id") UUID postId);
}
