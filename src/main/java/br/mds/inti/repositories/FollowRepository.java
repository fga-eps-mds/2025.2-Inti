package br.mds.inti.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.mds.inti.model.entity.Follow;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.model.entity.pk.FollowsPK;

public interface FollowRepository extends JpaRepository<Follow, FollowsPK> {

    @Query("select f from Follow f where f.followerProfile = :follower and f.followingProfile = :following")
    Optional<Follow> findFollowRelationship(@Param("follower") Profile followerProfile,
            @Param("following") Profile followingProfile);
}
