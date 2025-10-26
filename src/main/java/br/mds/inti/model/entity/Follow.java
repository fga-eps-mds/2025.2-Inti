package br.mds.inti.model.entity;

import br.mds.inti.model.entity.pk.FollowsPK;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;

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
