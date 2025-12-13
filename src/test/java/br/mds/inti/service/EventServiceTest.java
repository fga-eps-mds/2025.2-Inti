package br.mds.inti.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.mds.inti.model.dto.EventDetailResponse;
import br.mds.inti.model.dto.EventListResponse;
import br.mds.inti.model.dto.EventRequestDTO;
import br.mds.inti.model.dto.EventResponseDTO;
import br.mds.inti.model.dto.MyEvent;
import br.mds.inti.model.entity.Event;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.repositories.EventParticipantsRepository;
import br.mds.inti.repositories.EventRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private BlobService blobService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventParticipantsRepository eventParticipantsRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    void getMyEvents_shouldReturnMappedEventsWithImageAndDate() {
        Profile profile = buildProfile();
        Instant instantWithImage = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant instantWithoutImage = Instant.now().plus(2, ChronoUnit.DAYS);

        Event withImage = buildEvent("Evento 1", "banner.png", instantWithImage);
        Event withoutImage = buildEvent("Evento 2", null, instantWithoutImage);

        when(eventParticipantsRepository.findEventsByProfileId(profile.getId()))
                .thenReturn(List.of(withImage, withoutImage));

        List<MyEvent> myEvents = eventService.getMyEvents(profile);

        assertThat(myEvents).hasSize(2);

        MyEvent first = myEvents.get(0);
        LocalDateTime expectedFirstDate = instantWithImage.atZone(ZoneId.of("America/Sao_Paulo")).toLocalDateTime();
        assertThat(first.title()).isEqualTo("Evento 1");
        assertThat(first.imageUrl()).isEqualTo("/images/banner.png");
        assertThat(first.data()).isEqualTo(expectedFirstDate);
        assertThat(first.id()).isEqualTo(withImage.getId());

        MyEvent second = myEvents.get(1);
        LocalDateTime expectedSecondDate = instantWithoutImage.atZone(ZoneId.of("America/Sao_Paulo"))
                .toLocalDateTime();
        assertThat(second.title()).isEqualTo("Evento 2");
        assertThat(second.imageUrl()).isNull();
        assertThat(second.data()).isEqualTo(expectedSecondDate);
        assertThat(second.id()).isEqualTo(withoutImage.getId());

        verify(eventParticipantsRepository).findEventsByProfileId(profile.getId());
    }

    @Test
    void getMyEvents_whenRepositoryReturnsEmpty_shouldReturnEmptyList() {
        Profile profile = buildProfile();
        when(eventParticipantsRepository.findEventsByProfileId(profile.getId())).thenReturn(Collections.emptyList());

        List<MyEvent> myEvents = eventService.getMyEvents(profile);

        assertThat(myEvents).isEmpty();
        verify(eventParticipantsRepository).findEventsByProfileId(profile.getId());
    }

    @Test
    void getMyEvents_shouldExcludeFinishedEvents() {
        Profile profile = buildProfile();
        Event active = buildEvent("Ativo", null, Instant.now().plus(1, ChronoUnit.DAYS));
        Event finished = buildEvent("Finalizado", null, Instant.now().plus(2, ChronoUnit.DAYS));
        finished.setFinishedAt(Instant.now().minus(1, ChronoUnit.HOURS));

        when(eventParticipantsRepository.findEventsByProfileId(profile.getId()))
                .thenReturn(List.of(active, finished));

        List<MyEvent> myEvents = eventService.getMyEvents(profile);

        assertThat(myEvents).hasSize(1);
        assertThat(myEvents.get(0).title()).isEqualTo("Ativo");
    }

    @Test
    void getEventsCreatedByOrganization_shouldReturnActiveEventsMappedAsMyEvent() {
        Profile organizer = buildProfile();
        Event event = buildEvent("Meu Evento", "banner.png", Instant.now().plus(3, ChronoUnit.DAYS));

        when(eventRepository.findActiveEventsByOrganizer(eq(organizer.getId()), any()))
                .thenReturn(List.of(event));

        List<MyEvent> result = eventService.getEventsCreatedByOrganization(organizer);

        assertThat(result).hasSize(1);
        MyEvent myEvent = result.get(0);
        assertThat(myEvent.title()).isEqualTo("Meu Evento");
        assertThat(myEvent.imageUrl()).isEqualTo("/images/banner.png");
        assertThat(myEvent.id()).isEqualTo(event.getId());
        verify(eventRepository).findActiveEventsByOrganizer(eq(organizer.getId()), any());
    }

    @Test
    void getEventsCreatedByOrganization_whenRepositoryReturnsEmpty_shouldReturnEmptyList() {
        Profile organizer = buildProfile();
        when(eventRepository.findActiveEventsByOrganizer(eq(organizer.getId()), any())).thenReturn(Collections.emptyList());

        List<MyEvent> result = eventService.getEventsCreatedByOrganization(organizer);

        assertThat(result).isEmpty();
        verify(eventRepository).findActiveEventsByOrganizer(eq(organizer.getId()), any());
    }

    @Test
    void getEventById_whenRegistered_shouldSetFlagTrue() {
        Profile profile = buildProfile();
        UUID eventId = UUID.randomUUID();
        Instant eventTime = Instant.now().plus(1, ChronoUnit.DAYS);
        Event event = buildDetailedEvent(eventId, "detail.png", eventTime);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventParticipantsRepository.existsByEventIdAndProfileId(eventId, profile.getId())).thenReturn(true);

        EventDetailResponse response = eventService.getEventById(eventId, profile);

        assertThat(response.registered()).isTrue();
        assertThat(response.imageUrl()).isEqualTo("/images/detail.png");
        assertThat(response.id()).isEqualTo(eventId);
        Profile organizer = event.getProfile();
        assertThat(response.organizerId()).isEqualTo(organizer.getId());
        assertThat(response.organizerUsername()).isEqualTo(organizer.getUsername());
        assertThat(response.organizerProfilePictureUrl()).isEqualTo(organizer.getProfilePictureUrl());
    }

    @Test
    void getEventById_whenNotRegistered_shouldSetFlagFalse() {
        Profile profile = buildProfile();
        UUID eventId = UUID.randomUUID();
        Event event = buildDetailedEvent(eventId, null, Instant.now().plus(2, ChronoUnit.DAYS));

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventParticipantsRepository.existsByEventIdAndProfileId(eventId, profile.getId())).thenReturn(false);

        EventDetailResponse response = eventService.getEventById(eventId, profile);

        assertThat(response.registered()).isFalse();
        assertThat(response.imageUrl()).isNull();
        Profile organizer = event.getProfile();
        assertThat(response.organizerId()).isEqualTo(organizer.getId());
        assertThat(response.organizerUsername()).isEqualTo(organizer.getUsername());
        assertThat(response.organizerProfilePictureUrl()).isEqualTo(organizer.getProfilePictureUrl());
    }

    @Test
    void getEventById_whenProfileIsNull_shouldNotQueryParticipants() {
        UUID eventId = UUID.randomUUID();
        Event event = buildDetailedEvent(eventId, null, Instant.now().plus(3, ChronoUnit.DAYS));

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        EventDetailResponse response = eventService.getEventById(eventId, null);

        assertThat(response.registered()).isFalse();
        verify(eventParticipantsRepository, never()).existsByEventIdAndProfileId(any(), any());
        Profile organizer = event.getProfile();
        assertThat(response.organizerId()).isEqualTo(organizer.getId());
        assertThat(response.organizerUsername()).isEqualTo(organizer.getUsername());
        assertThat(response.organizerProfilePictureUrl()).isEqualTo(organizer.getProfilePictureUrl());
    }

    @Test
    void getEventById_whenEventFinished_shouldThrowNotFound() {
        Profile profile = buildProfile();
        UUID eventId = UUID.randomUUID();
        Event event = buildDetailedEvent(eventId, null, Instant.now().plus(1, ChronoUnit.DAYS), true);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> eventService.getEventById(eventId, profile));

        assertThat(exception.getStatusCode().value()).isEqualTo(404);
        verify(eventParticipantsRepository, never()).existsByEventIdAndProfileId(any(), any());
    }

    @Test
    void createEvent_withImage_shouldUploadAndPersist() throws Exception {
        Profile profile = buildProfile();
        EventRequestDTO request = buildEventRequest();
        String blobName = "event-banner.png";
        UUID generatedId = UUID.randomUUID();

        when(blobService.uploadImage(profile.getId(), request.image())).thenReturn(blobName);
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event saved = invocation.getArgument(0);
            saved.setId(generatedId);
            return saved;
        });

        EventResponseDTO response = eventService.createEvent(profile, request);

        assertThat(response.id()).isEqualTo(generatedId);
        assertThat(response.message()).isEqualTo("Evento criado com sucesso");
        verify(blobService).uploadImage(profile.getId(), request.image());

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());
        Event persisted = captor.getValue();
        assertThat(persisted.getTitle()).isEqualTo(request.title());
        assertThat(persisted.getDescription()).isEqualTo(request.description());
        assertThat(persisted.getBlobName()).isEqualTo(blobName);
        assertThat(persisted.getProfile()).isEqualTo(profile);
        assertThat(persisted.getEventTime()).isEqualTo(request.eventTime());
        assertThat(persisted.getFinishedAt()).isNotNull();
    }

    @Test
    void getListEvent_shouldReturnOnlyActiveEventsWithMappedFields() {
        Event active = buildEvent("Ativo", "banner.png", Instant.now().plus(1, ChronoUnit.DAYS));
        Event inactive = buildEvent("Inativo", null, Instant.now().minus(2, ChronoUnit.DAYS));
        inactive.setFinishedAt(Instant.now().minus(1, ChronoUnit.MINUTES));

        when(eventRepository.findAll()).thenReturn(List.of(active, inactive));

        List<EventListResponse> responses = eventService.getListEvent();

        assertThat(responses).hasSize(1);
        EventListResponse response = responses.get(0);
        assertThat(response.title()).isEqualTo("Ativo");
        assertThat(response.imageUrl()).isEqualTo("/images/banner.png");
        assertThat(response.id()).isEqualTo(active.getId());
    }

    private Profile buildProfile() {
        Profile profile = new Profile();
        profile.setId(UUID.randomUUID());
        return profile;
    }

    private Event buildEvent(String title, String blobName, Instant eventTime) {
        Event event = new Event();
        event.setId(UUID.randomUUID());
        event.setTitle(title);
        event.setBlobName(blobName);
        event.setEventTime(eventTime);
        event.setProfile(buildOrganizerProfile());
        event.setFinishedAt(eventTime.plus(15, ChronoUnit.MINUTES));
        return event;
    }

    private Event buildDetailedEvent(UUID id, String blobName, Instant eventTime) {
        return buildDetailedEvent(id, blobName, eventTime, false);
    }

    private Event buildDetailedEvent(UUID id, String blobName, Instant eventTime, boolean finished) {
        Event event = new Event();
        event.setId(id);
        event.setTitle("Detalhe");
        event.setBlobName(blobName);
        event.setEventTime(eventTime);
        event.setProfile(buildOrganizerProfile());
        event.setDescription("Descricao");
        event.setStreetAddress("Rua 1");
        event.setAdministrativeRegion("Centro");
        event.setCity("Brasilia");
        event.setState("DF");
        event.setReferencePoint("Praca");
        event.setLatitude(BigDecimal.ONE);
        event.setLongitude(BigDecimal.TEN);
        Instant finishedAt = finished ? Instant.now().minus(1, ChronoUnit.HOURS)
                : eventTime.plus(15, ChronoUnit.MINUTES);
        event.setFinishedAt(finishedAt);
        return event;
    }

    private EventRequestDTO buildEventRequest() {
        MockMultipartFile image = new MockMultipartFile("image", "banner.png", "image/png", "data".getBytes());
        return new EventRequestDTO(
                "Show",
                Instant.now().plus(1, ChronoUnit.DAYS),
                "Descricao",
                image,
                "Rua 1",
                "Centro",
                "Brasilia",
                "DF",
                "Perto",
                BigDecimal.ONE,
                BigDecimal.TEN);
    }

    private Profile buildOrganizerProfile() {
        Profile organizer = new Profile();
        organizer.setId(UUID.randomUUID());
        organizer.setUsername("org-" + organizer.getId().toString().substring(0, 8));
        organizer.setProfilePictureUrl("https://images.example/" + organizer.getId());
        return organizer;
    }

    @Test
    void deleteAllEvents_shouldDeleteBlobsParticipantsAndEvents() {
        Event eventWithBlob = buildEvent("Evento 1", "blob-a.png", Instant.now().plus(1, ChronoUnit.DAYS));
        Event eventWithoutBlob = buildEvent("Evento 2", null, Instant.now().plus(2, ChronoUnit.DAYS));

        when(eventRepository.findAll()).thenReturn(List.of(eventWithBlob, eventWithoutBlob));

        eventService.deleteAllEvents();

        verify(blobService).deleteImage("blob-a.png");
        verify(eventParticipantsRepository).deleteAllInBatch();
        verify(eventRepository).deleteAllInBatch();
    }

    @Test
    void deleteAllEvents_whenRepositoryReturnsEmpty_shouldDoNothing() {
        when(eventRepository.findAll()).thenReturn(Collections.emptyList());

        eventService.deleteAllEvents();

        verify(blobService, never()).deleteImage(any());
        verify(eventParticipantsRepository, never()).deleteAllInBatch();
        verify(eventRepository, never()).deleteAllInBatch();
    }

    @Test
    void deleteAllEvents_whenBlobMissing_shouldIgnoreNotFound() {
        Event event = buildEvent("Evento", "missing.png", Instant.now().plus(3, ChronoUnit.DAYS));
        when(eventRepository.findAll()).thenReturn(List.of(event));
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND)).when(blobService).deleteImage("missing.png");

        eventService.deleteAllEvents();

        verify(eventParticipantsRepository).deleteAllInBatch();
        verify(eventRepository).deleteAllInBatch();
    }
}
