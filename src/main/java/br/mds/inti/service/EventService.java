package br.mds.inti.service;

import br.mds.inti.model.dto.*;
import br.mds.inti.model.dto.EventResponseDTO;
import br.mds.inti.model.entity.Event;
import br.mds.inti.model.entity.EventParticipant;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.model.entity.pk.EventParticipantPK;
import br.mds.inti.repositories.EventParticipantsRepository;
import br.mds.inti.repositories.EventRepository;
import br.mds.inti.repositories.ProfileRepository;
import br.mds.inti.service.exception.EntityNotFoundException;
import br.mds.inti.service.exception.EventParticipantAlreadyExistsException;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventService {

    @Autowired
    BlobService blobService;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    ProfileRepository profileRepository;

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
        if (event.getEventTime() != null) {
            event.setFinishedAt(event.getEventTime().plus(15, ChronoUnit.MINUTES));
        }

        eventRepository.save(event);

        return new EventResponseDTO(event.getId(), EVENTO_CRIADO);
    }

    public EventDetailResponse getEventById(UUID eventId, Profile profile) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, EVENTO_NAO_ENCONTRADO));

        if (!isEventActive(event)) {
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
        Profile organizer = event.getProfile();
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
                registered,
                organizer != null ? organizer.getId() : null,
                organizer != null ? organizer.getUsername() : null,
                organizer != null ? organizer.getProfilePictureUrl() : null);
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
                .filter(this::isEventActive)
                .map(event -> new EventListResponse(event.getTitle(), generateImageUrl(event.getBlobName()),
                        event.getEventTime().atZone(ZoneId.of("America/Sao_Paulo")).toLocalDateTime(), event.getId()))
                .collect(Collectors.toList());
        return response;
    }

    public List<MyEvent> getMyEvents(Profile profile) {
        List<Event> events = eventParticipantsRepository.findEventsByProfileId(profile.getId());

        return events.stream()
                .filter(this::isEventActive)
                .map(this::toMyEvent)
                .toList();
    }

    public List<MyEvent> getEventsCreatedByOrganization(Profile profile) {
        List<Event> events = eventRepository.findActiveEventsByOrganizer(profile.getId(), Instant.now());

        return events.stream()
                .filter(this::isEventActive)
                .map(this::toMyEvent)
                .toList();
    }

    public List<FollowingAttendeeDTO> getEventsFromFollowing(Profile profile, UUID eventId) {
        Optional<List<FollowingAttendeeDTO>> followedByProfile = profileRepository.findFriendsGoingToEvent(eventId,
                profile.getId());
        if (followedByProfile.isEmpty() || followedByProfile.get().isEmpty()) {
            return List.of();
        }

        return followedByProfile.get();
    }

    @Transactional
    public void deleteAllEvents() {
        List<Event> events = eventRepository.findAll();
        if (events.isEmpty()) {
            return;
        }

        events.stream()
                .map(Event::getBlobName)
                .filter(blobName -> blobName != null && !blobName.isBlank())
                .forEach(this::deleteBlobSafely);

        eventParticipantsRepository.deleteAllInBatch();
        eventRepository.deleteAllInBatch();
    }

    private void deleteBlobSafely(String blobName) {
        try {
            blobService.deleteImage(blobName);
        } catch (ResponseStatusException ex) {
            if (!HttpStatus.NOT_FOUND.equals(ex.getStatusCode())) {
                throw ex;
            }
        }
    }

    private boolean isEventActive(Event event) {
        Instant finishedAt = event.getFinishedAt();
        return finishedAt == null || finishedAt.isAfter(Instant.now());
    }

    private MyEvent toMyEvent(Event event) {
        return new MyEvent(
                event.getId(),
                event.getTitle(),
                generateImageUrl(event.getBlobName()),
                event.getEventTime().atZone(ZoneId.of("America/Sao_Paulo")).toLocalDateTime());
    }
}
