package br.mds.inti.service;

import br.mds.inti.model.dto.EventDetailResponse;
import br.mds.inti.model.dto.EventRequestDTO;
import br.mds.inti.model.dto.EventResponseDTO;
import br.mds.inti.model.dto.LocalAddress;
import br.mds.inti.model.entity.Event;
import br.mds.inti.model.entity.EventParticipant;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.model.entity.pk.EventParticipantPK;
import br.mds.inti.repositories.EventParticipantsRepository;
import br.mds.inti.repositories.EventRepository;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Service
public class EventService {

    @Autowired
    BlobService blobService;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    EventParticipantsRepository eventParticipantsRepository;

    final String EVENTO_CRIADO = "Evento criado com sucesso";
    final String EVENTO_NAO_ENCONTRADO = "Evento não encontrado";

    public EventResponseDTO createEvent(@NotNull Profile profile, @NotNull EventRequestDTO eventRequestDTO) throws IOException {
        Event event = new Event();
        event.setProfile(profile);
        event.setTitle(eventRequestDTO.title());

        String blobName = null;
        if(eventRequestDTO.image() != null) {
            blobName = blobService.uploadImage(profile.getId(), eventRequestDTO.image());
        }

        event.setBlobName(blobName);
        event.setEventTime(eventRequestDTO.eventTime());
        event.setDescription(eventRequestDTO.description());
        event.setStreetAddress(eventRequestDTO.streetAddress());
        event.setAdministrativeRegion(eventRequestDTO.administrativeRegion());
        event.setCity(eventRequestDTO.city());
        event.setState(eventRequestDTO.state());
        event.setReferencePoint(eventRequestDTO.referencePoint());
        event.setLatitude(eventRequestDTO.latitude());
        event.setLongitude(eventRequestDTO.longitude());
        event.setCreatedAt(Instant.now());

        eventRepository.save(event);

        return new EventResponseDTO(event.getId(), EVENTO_CRIADO);
    }

    public EventDetailResponse getEventById(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, EVENTO_NAO_ENCONTRADO));
        
        return convertToDetailResponse(event);
    }

    public String generateImageUrl(String blobName) {
        if (blobName == null || blobName.isEmpty()) {
            return null;
        }
        return "/images/" + blobName;
    }

    private EventDetailResponse convertToDetailResponse(Event event) {
        return new EventDetailResponse(
                event.getId(),
                event.getTitle(),
                "/images/" + event.getBlobName(),
                event.getEventTime(),
                event.getDescription(),
                new LocalAddress(event.getStreetAddress(), event.getAdministrativeRegion(), event.getCity(),
                        event.getState(), event.getReferencePoint()),
                event.getLatitude(),
                event.getLongitude(),
                event.getFinishedAt());
    }
    
    public EventParticipant eventInscription(UUID eventid, Profile profile) {

        Event event = eventRepository.findById(eventid)
                .orElseThrow(() -> new RuntimeException(EVENTO_NAO_ENCONTRADO));

        EventParticipant eventParticipant = new EventParticipant();
        eventParticipant.setEvent(event);
        eventParticipant.setProfile(profile);
        eventParticipant.setCreatedAt(Instant.now());

        EventParticipantPK eventParticipantPK = new EventParticipantPK();

        eventParticipantPK.setEventId(event.getId());
        eventParticipantPK.setProfileId(profile.getId());

        eventParticipant.setId(eventParticipantPK);

        return eventParticipantsRepository.save(eventParticipant);
    }
    
    public void deleteInscription(EventParticipantPK eventParticipantId) {
        EventParticipant eventParticipant2 = eventParticipantsRepository.findById(eventParticipantId)
                .orElseThrow(() -> new RuntimeException("Erro ao deletar o profile do evento"));

        eventParticipantsRepository.delete(eventParticipant2);
    }
}