package br.mds.inti.repositories;

import br.mds.inti.model.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    Optional<Profile> findByUsername(String username);

    Optional<Profile> findByEmail(String email);

    @Query("SELECT COUNT(u) > 0 FROM Profile u WHERE u.username = :username")
    Boolean findIfUsernameIsUsed(@Param("username") String username);
}
