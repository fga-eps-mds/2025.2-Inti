package br.mds.inti.models;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
