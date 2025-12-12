package br.mds.inti.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import br.mds.inti.model.dto.EventDetailResponse;
import br.mds.inti.model.dto.EventListResponse;
import br.mds.inti.model.dto.EventRequestDTO;
import br.mds.inti.model.dto.EventResponseDTO;
import br.mds.inti.model.dto.LocalAddress;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.model.entity.pk.EventParticipantPK;
import br.mds.inti.model.enums.ProfileType;
import br.mds.inti.service.EventService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

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

    @Test
    void createEvent_withOrganizationProfile_shouldReturnCreatedResponse() throws Exception {
        Profile organization = buildProfile(ProfileType.organization);
        mockSecurityContext(organization);
        EventRequestDTO request = buildEventRequest();
        EventResponseDTO serviceResponse = new EventResponseDTO(UUID.randomUUID(), "Evento criado");
        when(eventService.createEvent(organization, request)).thenReturn(serviceResponse);

        ResponseEntity<EventResponseDTO> response = eventController.createEvent(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(serviceResponse);
        verify(eventService).createEvent(organization, request);
    }

    @Test
    void createEvent_withNonOrganizationProfile_shouldThrowForbidden() throws Exception {
        Profile userProfile = buildProfile(ProfileType.user);
        mockSecurityContext(userProfile);
        EventRequestDTO request = buildEventRequest();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> eventController.createEvent(request));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(eventService, never()).createEvent(any(Profile.class), any(EventRequestDTO.class));
    }

    @Test
    void listEvents_shouldReturnServiceResponse() {
        List<EventListResponse> serviceResponse = List.of(
                new EventListResponse("Show", "/images/show.png", LocalDateTime.now(), UUID.randomUUID()));
        when(eventService.getListEvent()).thenReturn(serviceResponse);

        ResponseEntity<List<EventListResponse>> response = eventController.listEvents();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(serviceResponse);
        verify(eventService).getListEvent();
    }

    @Test
    void deleteAllEvents_withOrganizationProfile_shouldInvokeService() {
        Profile organization = buildProfile(ProfileType.organization);
        mockSecurityContext(organization);

        ResponseEntity<Void> response = eventController.deleteAllEvents();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(eventService).deleteAllEvents();
    }

    @Test
    void deleteAllEvents_withNonOrganizationProfile_shouldThrowForbidden() {
        Profile userProfile = buildProfile(ProfileType.user);
        mockSecurityContext(userProfile);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> eventController.deleteAllEvents());

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(eventService, never()).deleteAllEvents();
    }

    private void mockSecurityContext(Object principal) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(context);
    }

    private Profile buildProfile() {
        return buildProfile(ProfileType.user);
    }

    private Profile buildProfile(ProfileType type) {
        Profile profile = new Profile();
        profile.setId(UUID.randomUUID());
        profile.setUsername("testuser");
        profile.setType(type);
        return profile;
    }

    private EventRequestDTO buildEventRequest() {
        MockMultipartFile image = new MockMultipartFile("image", "banner.png", "image/png", "data".getBytes());
        return new EventRequestDTO(
                "Titulo",
                Instant.now().plusSeconds(3600),
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
                registered,
                UUID.randomUUID(),
                "orgTest",
                "https://images/org.png");
    }
}
