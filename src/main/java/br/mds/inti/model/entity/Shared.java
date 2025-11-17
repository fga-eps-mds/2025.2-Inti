package br.mds.inti.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "shareds")
public class Shared {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "profile_sharing_id", nullable = false)
    private Profile profileSharing;

    @ManyToOne
    @JoinColumn(name = "profile_shared_id", nullable = false)
    private Profile profileShared;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = true)
    private Post postId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
