package br.mds.inti.model.entity;

import br.mds.inti.model.entity.pk.EventParticipantPK;
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
