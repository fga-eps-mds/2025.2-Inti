package br.mds.inti.repositories;

import br.mds.inti.model.entity.Like;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.model.entity.pk.LikePk;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LikeRepository extends JpaRepository<Like, LikePk> {

    Optional<Like> findByProfileAndPostId(Profile profile, UUID postId);

    @Query("select l.id.postId from Like l where l.profile.id = :profileId and l.id.postId in :postIds")
    Set<UUID> findLikedPostIds(@Param("profileId") UUID profileId, @Param("postIds") List<UUID> postIds);
}