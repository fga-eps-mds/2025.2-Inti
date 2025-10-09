package br.mds.inti.models;

import java.time.Instant;

import br.mds.inti.models.pk.EventParticipantPK;
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
@Table(name = "event_participants")
public class EventParticipant {

    @EmbeddedId
    private EventParticipantPK id;

    @ManyToOne
    @MapsId("profileId")
    @JoinColumn(name = "profile_id")
    private Profile profile;

    @ManyToOne
    @MapsId("eventId") // conex
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
