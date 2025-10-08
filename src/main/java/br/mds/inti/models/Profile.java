package br.mds.inti.models;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import br.mds.inti.models.ENUM.ProfileType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "profiles")
public class Profile implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "username", unique = true, length = 50)
    private String username;

    @Column(name = "name", length = 150)
    private String name;

    @Column(unique = true, name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "public_email", nullable = true, length = 255)
    private String publicEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "type", length = 50)
    private ProfileType type;

    @Column(name = "followers_count", nullable = true)
    private Integer followersCount;

    @Column(name = "following_count", nullable = true)
    private Integer followingCount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = true)
    private Instant updatedAt;

    @Column(name = "deleted_at", nullable = true)
    private Instant deletedAt;

    @OneToMany(mappedBy = "profile")
    private List<EventParticipant> eventParticipants = new ArrayList<>();

    @OneToMany(mappedBy = "profile")
    private List<ArtistProducts> artistProducts = new ArrayList<>();

    @OneToMany(mappedBy = "followerProfile")
    private List<Follow> followerProfiles = new ArrayList<>();

    @OneToMany(mappedBy = "followingProfile")
    private List<Follow> followingProfiles = new ArrayList<>();

    @Override
    public boolean isAccountNonExpired() {

        return deletedAt == null;
    }

    @Override
    public boolean isAccountNonLocked() {
        return deletedAt == null;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return deletedAt == null;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        if (type == ProfileType.organization)
            return List.of(new SimpleGrantedAuthority("ROLE_ORGANIZATION"));
        else
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
