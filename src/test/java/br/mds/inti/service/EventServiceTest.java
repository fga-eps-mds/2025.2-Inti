package br.mds.inti.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.mds.inti.model.dto.MyEvent;
import br.mds.inti.model.entity.Event;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.repositories.EventParticipantsRepository;
import br.mds.inti.repositories.EventRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
