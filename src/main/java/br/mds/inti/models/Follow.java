package br.mds.inti.models;

import java.time.Instant;

import br.mds.inti.models.pk.FollowsPK;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
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
@Table(name = "follows")
public class Follow {

    @EmbeddedId
    private FollowsPK id;

    @ManyToOne
    @JoinColumn(name = "follower_profile_id", nullable = false)
    @MapsId("followerProfileId")
    private Profile followerProfile;

    @ManyToOne
    @JoinColumn(name = "following_profile_id", nullable = false)
    @MapsId("followingProfileId")
    private Profile followingProfile;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

}
