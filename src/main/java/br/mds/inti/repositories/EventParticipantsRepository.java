package br.mds.inti.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.mds.inti.model.entity.EventParticipant;
import br.mds.inti.model.entity.pk.EventParticipantPK;
import br.mds.inti.model.entity.Event;
import java.util.List;

public interface EventParticipantsRepository extends JpaRepository<EventParticipant, EventParticipantPK> {

    @Query("SELECT CASE WHEN COUNT(ep) > 0 THEN true ELSE false END FROM EventParticipant ep " +
            "WHERE ep.event.id = :eventId AND ep.profile.id = :profileId")
    boolean existsByEventIdAndProfileId(@Param("eventId") UUID eventId, @Param("profileId") UUID profileId);

    @Query("SELECT ep FROM EventParticipant ep WHERE ep.event.id = :eventId AND ep.profile.id = :profileId")
    Optional<EventParticipant> findByEventIdAndProfileId(@Param("eventId") UUID eventId,
            @Param("profileId") UUID profileId);

    @Query("SELECT ep.event FROM EventParticipant ep WHERE ep.profile.id = :profileId")
    List<Event> findEventsByProfileId(@Param("profileId") UUID profileId);
}