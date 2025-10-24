package br.mds.inti.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "blob_name", nullable = true, length = 255)
    private String blobName;

    @Column(name = "event_time", nullable = false)
    private Instant eventTime;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "street_address", nullable = true, length = 150)
    private String streetAddress;

    @Column(name = "administrative_region", nullable = true, length = 150)
    private String administrativeRegion;

    @Column(name = "city", nullable = true, length = 150)
    private String city;

    @Column(name = "state", nullable = true, length = 150)
    private String state;

    @Column(name = "reference_point", nullable = true, length = 255)
    private String referencePoint;

    @Column(name = "latitude", nullable = true, precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = true, precision = 11, scale = 6)
    private BigDecimal longitude;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "finished_at", nullable = true)
    private Instant finishedAt;

    @OneToMany(mappedBy = "event")
    private List<EventParticipant> eventParticipants = new ArrayList<>();
}
