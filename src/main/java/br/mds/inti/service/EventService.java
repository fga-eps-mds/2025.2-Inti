package br.mds.inti.service;

import br.mds.inti.model.dto.EventDetailResponse;
import br.mds.inti.model.dto.EventParticipantResponse;
import br.mds.inti.model.dto.EventListResponse;
import br.mds.inti.model.dto.EventRequestDTO;
import br.mds.inti.model.dto.EventResponseDTO;
import br.mds.inti.model.dto.LocalAddress;
import br.mds.inti.model.dto.MyEvent;
import br.mds.inti.model.entity.Event;
import br.mds.inti.model.entity.EventParticipant;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.model.entity.pk.EventParticipantPK;
import br.mds.inti.repositories.EventParticipantsRepository;
import br.mds.inti.repositories.EventRepository;
import br.mds.inti.service.exceptions.EntityNotFoundException;
import br.mds.inti.service.exceptions.EventParticipantAlreadyExistsException;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public EventResponseDTO createEvent(@NotNull Profile profile, @NotNull EventRequestDTO eventRequestDTO)
            throws IOException {
        Event event = new Event();
        event.setProfile(profile);
        event.setTitle(eventRequestDTO.title());

        String blobName = null;
        if (eventRequestDTO.image() != null) {
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

    public EventDetailResponse getEventById(UUID eventId, Profile profile) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, EVENTO_NAO_ENCONTRADO));

        if (event.getFinishedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, EVENTO_NAO_ENCONTRADO);
        }

        boolean registered = profile != null && eventParticipantsRepository
                .existsByEventIdAndProfileId(event.getId(), profile.getId());

        return convertToDetailResponse(event, registered);
    }

    public String generateImageUrl(String blobName) {
        if (blobName == null || blobName.isEmpty()) {
            return null;
        }
        return "/images/" + blobName;
    }

    private EventDetailResponse convertToDetailResponse(Event event, boolean registered) {
        return new EventDetailResponse(
                event.getId(),
                event.getTitle(),
                generateImageUrl(event.getBlobName()),
                event.getEventTime(),
                event.getDescription(),
                new LocalAddress(event.getStreetAddress(), event.getAdministrativeRegion(), event.getCity(),
                        event.getState(), event.getReferencePoint()),
                event.getLatitude(),
                event.getLongitude(),
                event.getFinishedAt(),
                registered);
    }

    public EventParticipantResponse eventInscription(UUID eventid, Profile profile) {

        Event event = eventRepository.findById(eventid)
                .orElseThrow(() -> new RuntimeException(EVENTO_NAO_ENCONTRADO));

        EventParticipantPK eventParticipantPK = new EventParticipantPK(event.getId(), profile.getId());

        if (eventParticipantsRepository.existsByEventIdAndProfileId(event.getId(), profile.getId())) {
            throw new EventParticipantAlreadyExistsException("Você já está inscrito neste evento");
        }

        EventParticipant eventParticipant = new EventParticipant(
                eventParticipantPK,
                profile,
                event,
                Instant.now());

        eventParticipantsRepository.save(eventParticipant);

        return new EventParticipantResponse(event.getId(), profile.getId(), eventParticipant.getCreatedAt());
    }

    public void deleteInscription(EventParticipantPK eventParticipantId) {
        if (!eventParticipantsRepository.existsById(eventParticipantId)) {
            throw new EntityNotFoundException("Inscrição não encontrada");
        }
        eventParticipantsRepository.deleteById(eventParticipantId);
    }

    public List<EventListResponse> getListEvent() {
        List<EventListResponse> response = eventRepository.findAll().stream()
            .filter(event -> event.getFinishedAt() == null)
            .map(event -> new EventListResponse(event.getTitle(), generateImageUrl(event.getBlobName()),
                event.getEventTime().atZone(ZoneId.of("America/Sao_Paulo")).toLocalDateTime(), event.getId()))
                .collect(Collectors.toList());
        return response;
    }

    public List<MyEvent> getMyEvents(Profile profile) {
        List<Event> events = eventParticipantsRepository.findEventsByProfileId(profile.getId());

        return events.stream()
            .filter(event -> event.getFinishedAt() == null)
                .map(event -> new MyEvent(
                        event.getId(),
                        event.getTitle(),
                        generateImageUrl(event.getBlobName()),
                        event.getEventTime().atZone(ZoneId.of("America/Sao_Paulo")).toLocalDateTime()))
                .toList();
    }
}
