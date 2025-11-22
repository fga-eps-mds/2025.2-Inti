package br.mds.inti.model.entity;

import br.mds.inti.model.enums.ProfileType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

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

    @Column(name = "profile_picture_url", length = 255, nullable = true)
    private String profilePictureUrl;

    @Column(name = "bio", length = 255, nullable = true)
    private String bio;

    @Column(name = "public_email", nullable = true, length = 255)
    private String publicEmail;

    @Column(name = "phone", nullable = true)
    private String phone;

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

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "profile")
    private List<Membership> profiles = new ArrayList<>();

    @OneToMany(mappedBy = "organization")
    private List<Membership> organizations = new ArrayList<>();

    @OneToMany(mappedBy = "profile")
    private List<Like> likes = new ArrayList<>();

    @OneToMany(mappedBy = "profileSharing")
    private List<Shared> sharings = new ArrayList<>();

    @OneToMany(mappedBy = "profileShared")
    private List<Shared> shareds = new ArrayList<>();

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
        return true;
    }

    @Override
    public boolean isEnabled() {
        return deletedAt == null;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        if (type == ProfileType.organization)
            return List.of(new SimpleGrantedAuthority("ROLE_ORGANIZATION"));
        else
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
