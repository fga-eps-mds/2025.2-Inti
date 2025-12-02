package br.mds.inti.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import br.mds.inti.model.dto.EventDetailResponse;
import br.mds.inti.model.dto.LocalAddress;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.model.entity.pk.EventParticipantPK;
import br.mds.inti.service.EventService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getEventById_withAuthenticatedProfile_shouldPassProfileToService() {
        Profile profile = buildProfile();
        UUID eventId = UUID.randomUUID();
        EventDetailResponse serviceResponse = buildEventDetailResponse(eventId, true);

        mockSecurityContext(profile);
        when(eventService.getEventById(eventId, profile)).thenReturn(serviceResponse);

        ResponseEntity<EventDetailResponse> response = eventController.getEventById(eventId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(serviceResponse);
        verify(eventService).getEventById(eventId, profile);
    }

    @Test
    void getEventById_withoutProfile_shouldPassNullToService() {
        UUID eventId = UUID.randomUUID();
        EventDetailResponse serviceResponse = buildEventDetailResponse(eventId, false);

        mockSecurityContext("anonymousUser");
        when(eventService.getEventById(eventId, null)).thenReturn(serviceResponse);

        ResponseEntity<EventDetailResponse> response = eventController.getEventById(eventId);

        assertThat(response.getBody()).isEqualTo(serviceResponse);
        verify(eventService).getEventById(eventId, null);
    }

    @Test
    void deleteInscription_shouldUseAuthenticatedProfileAndReturnNoContent() {
        Profile profile = buildProfile();
        UUID eventId = UUID.randomUUID();
        EventParticipantPK expectedPk = new EventParticipantPK(profile.getId(), eventId);

        mockSecurityContext(profile);

        ResponseEntity<Void> response = eventController.deleteInscription(eventId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(eventService).deleteInscription(expectedPk);
    }

    private void mockSecurityContext(Object principal) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(context);
    }

    private Profile buildProfile() {
        Profile profile = new Profile();
        profile.setId(UUID.randomUUID());
        profile.setUsername("testuser");
        return profile;
    }

    private EventDetailResponse buildEventDetailResponse(UUID eventId, boolean registered) {
        return new EventDetailResponse(
                eventId,
                "Sample Event",
                "/images/sample.png",
                Instant.now(),
                "Descricao",
                new LocalAddress("Rua 1", "Centro", "Brasilia", "DF", "Ponto"),
                BigDecimal.ONE,
                BigDecimal.TEN,
                Instant.now().plusSeconds(3600),
                registered);
    }
}
