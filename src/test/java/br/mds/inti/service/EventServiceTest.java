package br.mds.inti.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.mds.inti.model.dto.EventDetailResponse;
import br.mds.inti.model.dto.MyEvent;
import br.mds.inti.model.entity.Event;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.repositories.EventParticipantsRepository;
import br.mds.inti.repositories.EventRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
        Instant instantWithImage = Instant.parse("2025-06-10T12:00:00Z");
        Instant instantWithoutImage = Instant.parse("2025-07-15T15:30:00Z");

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
        Event active = buildEvent("Ativo", null, Instant.parse("2025-08-01T10:00:00Z"));
        Event finished = buildEvent("Finalizado", null, Instant.parse("2025-07-01T10:00:00Z"));
        finished.setFinishedAt(Instant.now());

        when(eventParticipantsRepository.findEventsByProfileId(profile.getId()))
                .thenReturn(List.of(active, finished));

        List<MyEvent> myEvents = eventService.getMyEvents(profile);

        assertThat(myEvents).hasSize(1);
        assertThat(myEvents.get(0).title()).isEqualTo("Ativo");
    }

    @Test
    void getEventById_whenRegistered_shouldSetFlagTrue() {
        Profile profile = buildProfile();
        UUID eventId = UUID.randomUUID();
        Instant eventTime = Instant.parse("2025-09-01T10:15:30Z");
        Event event = buildDetailedEvent(eventId, "detail.png", eventTime);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventParticipantsRepository.existsByEventIdAndProfileId(eventId, profile.getId())).thenReturn(true);

        EventDetailResponse response = eventService.getEventById(eventId, profile);

        assertThat(response.registered()).isTrue();
        assertThat(response.imageUrl()).isEqualTo("/images/detail.png");
        assertThat(response.id()).isEqualTo(eventId);
    }

    @Test
    void getEventById_whenNotRegistered_shouldSetFlagFalse() {
        Profile profile = buildProfile();
        UUID eventId = UUID.randomUUID();
        Event event = buildDetailedEvent(eventId, null, Instant.parse("2025-10-05T08:00:00Z"));

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventParticipantsRepository.existsByEventIdAndProfileId(eventId, profile.getId())).thenReturn(false);

        EventDetailResponse response = eventService.getEventById(eventId, profile);

        assertThat(response.registered()).isFalse();
        assertThat(response.imageUrl()).isNull();
    }

    @Test
    void getEventById_whenProfileIsNull_shouldNotQueryParticipants() {
        UUID eventId = UUID.randomUUID();
        Event event = buildDetailedEvent(eventId, null, Instant.parse("2025-11-20T18:00:00Z"));

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        EventDetailResponse response = eventService.getEventById(eventId, null);

        assertThat(response.registered()).isFalse();
        verify(eventParticipantsRepository, never()).existsByEventIdAndProfileId(any(), any());
    }

    @Test
    void getEventById_whenEventFinished_shouldThrowNotFound() {
        Profile profile = buildProfile();
        UUID eventId = UUID.randomUUID();
        Event event = buildDetailedEvent(eventId, null, Instant.parse("2025-12-01T12:00:00Z"), true);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> eventService.getEventById(eventId, profile));

        assertThat(exception.getStatusCode().value()).isEqualTo(404);
        verify(eventParticipantsRepository, never()).existsByEventIdAndProfileId(any(), any());
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
        event.setDescription("Descricao");
        event.setStreetAddress("Rua 1");
        event.setAdministrativeRegion("Centro");
        event.setCity("Brasilia");
        event.setState("DF");
        event.setReferencePoint("Praca");
        event.setLatitude(BigDecimal.ONE);
        event.setLongitude(BigDecimal.TEN);
        if (finished) {
            event.setFinishedAt(eventTime.plusSeconds(3600));
        }
        return event;
    }
}
