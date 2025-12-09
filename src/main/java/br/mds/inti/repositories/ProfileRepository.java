package br.mds.inti.repositories;

import br.mds.inti.model.dto.EventFollowingDTO;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.model.enums.ProfileType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    Optional<Profile> findByUsername(String username);

    Optional<Profile> findByEmail(String email);

    @Query("SELECT COUNT(u) > 0 FROM Profile u WHERE u.username = :username")
    Boolean findIfUsernameIsUsed(@Param("username") String username);

    @Query("select p.id from Profile p where p.type = :type order by random()")
    List<UUID> findByOrganization(@Param("type") ProfileType type, Pageable pageable);

    @Query("""
        SELECT
            p.id as profileId,
            p.username as username,
            p.profilePictureUrl as profilePictureUrl
        FROM Profile p
        JOIN EventParticipant ep ON ep.profile.id = p.id
        JOIN Follow f ON f.followingProfile.id = p.id
        WHERE ep.event.id = :eventId
        AND f.followerProfile.id = :myUserId
   """)
    Optional<List<EventFollowingDTO>> findFriendsGoingToEvent(
        @Param("eventId") UUID eventId,
        @Param("myUserId") UUID myUserId
    );
}
