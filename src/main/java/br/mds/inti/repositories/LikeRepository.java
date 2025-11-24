package br.mds.inti.repositories;

import br.mds.inti.model.entity.Like;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.model.entity.pk.LikePk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LikeRepository extends JpaRepository<Like, LikePk> {

    Optional<Like> findByProfileAndPostId(Profile profile, UUID postId);
}