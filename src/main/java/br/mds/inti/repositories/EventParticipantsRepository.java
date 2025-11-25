package br.mds.inti.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import br.mds.inti.model.entity.EventParticipant;
import br.mds.inti.model.entity.pk.EventParticipantPK;

public interface EventParticipantsRepository extends JpaRepository<EventParticipant, EventParticipantPK> {
}