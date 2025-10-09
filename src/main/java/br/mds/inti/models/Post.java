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
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profileID;

    @Column(name = "img_link", nullable = true, length = 255)
    private String imgLink;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "likes_count", nullable = true)
    private Integer likesCount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
