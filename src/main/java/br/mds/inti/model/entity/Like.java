package br.mds.inti.model.entity;

import br.mds.inti.model.entity.pk.LikePk;
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
@Table(name = "likes")
public class Like {
    @EmbeddedId
    private LikePk id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    @MapsId("userId")
    private Profile profile;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = true)
    @MapsId("postId")
    private Post post;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

}
