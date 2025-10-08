package br.mds.inti.models;

import java.math.BigDecimal;
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

    @Column(name = "img_link", nullable = true, length = 255)
    private String imgLink;

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
}
