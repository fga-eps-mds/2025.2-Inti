package br.mds.inti.repositories;

import br.mds.inti.model.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    @Query("SELECT e FROM Event e WHERE e.finishedAt IS NULL ORDER BY e.eventTime ASC")
    Page<Event> findActiveEvents(Pageable pageable);

    @Query("SELECT e FROM Event e ORDER BY e.createdAt DESC")
    Page<Event> findAllEvents(Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.profile.id = :profileId AND (e.finishedAt IS NULL OR e.finishedAt > :currentTime) ORDER BY e.eventTime ASC")
    List<Event> findActiveEventsByOrganizer(UUID profileId, Instant currentTime);

}
