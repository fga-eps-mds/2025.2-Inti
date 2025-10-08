package br.mds.inti.models;

import java.time.Instant;

import br.mds.inti.models.pk.SharedsPk;
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
@Table(name = "shareds")
public class Shareds {
    @EmbeddedId
    private SharedsPk id;

    @ManyToOne
    @JoinColumn(name = "profile_sharing_id", nullable = false)
    @MapsId("profileSharingId")
    private Profile profileSharingId;

    @ManyToOne
    @JoinColumn(name = "profile_shared_id", nullable = false)
    @MapsId("profileSharedId")
    private Profile profileSharedId;

    @Column(name = "posts_id", nullable = true)
    private Posts postId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
