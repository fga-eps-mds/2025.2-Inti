package br.mds.inti.repositories;

import br.mds.inti.model.entity.Post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {

    @Query("select p from Post p where p.profile.id = :profileId and p.deletedAt is null")
    Page<Post> findAllByProfileIdAndNotDeleted(@Param("profileId") UUID profileId, Pageable pageable);

    Optional<Post> findById(UUID id);

    @Modifying
    @Query("update Post p set p.deletedAt = CURRENT_TIMESTAMP where p.id = ?1")
    void softDeletePost(@Param(value = "id") UUID postId);

    // diferença dessa pra primeira query é por conta da ordenação por criação
    @Query("select p from Post p where p.profile.id in :userIds and p.deletedAt is null order by p.createdAt desc")
    List<Post> findByUserIdsAndNotDeleted(@Param("userIds") List<UUID> userIds, Pageable pageable);

    // Posts populares
    @Query("SELECT p FROM Post p WHERE p.deletedAt IS NULL AND p.likesCount > :minLikes ORDER BY p.likesCount DESC")
    List<Post> findPopularPosts(@Param("minLikes") int minLikes, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.profile.id NOT IN :excludedUserIds AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Post> findRecentPostsExcludingUsers(@Param("excludedUserIds") List<UUID> excludedUserIds, Pageable pageable);

    @Query("select p from Post p where p.profile.id in :organizationIds and p.deletedAt is null order by p.createdAt desc")
    List<Post> findPostByOrganizationAndNotDeleted(@Param("organizationIds") List<UUID> organizationIds, Pageable pageable);
}
