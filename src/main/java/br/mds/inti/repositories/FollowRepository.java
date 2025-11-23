package br.mds.inti.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    // IDs of profiles the given user follows
    @Query("select f.followingProfile.id from Follow f where f.followerProfile.id = :followerId")
    List<UUID> findFollowedUserIds(@Param("followerId") UUID followerId);

    // IDs of profiles that follow the given user
    @Query("select f.followerProfile.id from Follow f where f.followingProfile.id = :followingId")
    List<UUID> findFollowerIds(@Param("followingId") UUID followingId);

    // IDs of profiles followed by any of the given user IDs (second degree)
    @Query("select distinct f.followingProfile.id from Follow f where f.followerProfile.id in :userIds")
    List<UUID> findFollowedByUsers(@Param("userIds") List<UUID> userIds);

    // IDs of second degree connections (people followed by your followers,
    // excluding yourself and direct follows)
    @Query("""
                SELECT DISTINCT f2.followerProfile.id
                FROM Follow f1
                JOIN Follow f2 ON f1.followerProfile.id = f2.followingProfile.id
                WHERE f1.followingProfile.id = :profileId
                AND f2.followerProfile.id != :profileId
                AND f2.followerProfile.id NOT IN (
                    SELECT f3.followerProfile.id FROM Follow f3
                    WHERE f3.followingProfile.id = :profileId
                )
            """)
    List<UUID> findSecondDegreeConnectionIds(@Param("profileId") UUID profileId);
}
