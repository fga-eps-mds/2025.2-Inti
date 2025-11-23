package br.mds.inti.model.entity;

import br.mds.inti.model.entity.pk.LikePk;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
