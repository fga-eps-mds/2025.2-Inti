package br.mds.inti.models;

import java.time.Instant;

import br.mds.inti.models.pk.LikePk;
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
@Table(name = "likes")
public class Like {
    @EmbeddedId
    private LikePk id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    @MapsId("userId")
    private Profile user;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = true)
    @MapsId("postId")
    private Post post;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

}
