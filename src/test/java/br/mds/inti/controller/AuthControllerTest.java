package br.mds.inti.controller;

import br.mds.inti.model.dto.auth.LoginRequest;
import br.mds.inti.model.dto.auth.ProfileCreationResponse;
import br.mds.inti.model.dto.auth.RegisterRequest;
import br.mds.inti.model.dto.auth.LoginResponse;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.model.enums.ProfileType;
import br.mds.inti.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private ProfileCreationResponse mockProfileCreationResponse;

    @BeforeEach
    void setUp() {
        UUID profileId = UUID.randomUUID();
        mockProfileCreationResponse = new ProfileCreationResponse(
                profileId,
                "testuser",
                "Test User",
                "test@example.com",
                "generated-jwt-token",
                ProfileType.user,
                Instant.now());

        // Setup mock request context for URI building
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8080);
        request.setRequestURI("/auth/register");
        request.setContextPath("");
        ServletRequestAttributes attrs = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attrs);
    }

    @Test
    void register_ShouldReturnCreatedStatusWithProfileResponse() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "test@example.com",
                "Test User",
                "testuser",
                "password123",
                ProfileType.user);

        when(authService.register(request)).thenReturn(mockProfileCreationResponse);

        // Act
        ResponseEntity<ProfileCreationResponse> response = authController.register(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockProfileCreationResponse, response.getBody());
        assertNotNull(response.getHeaders().getLocation());
        assertTrue(
                response.getHeaders().getLocation().toString().contains(mockProfileCreationResponse.id().toString()));
        verify(authService).register(request);
    }

    @Test
    void register_WithOrganizationType_ShouldReturnCreatedStatusWithOrganizationProfile() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "org@example.com",
                "Test Organization",
                "testorg",
                "password123",
                ProfileType.organization);

        ProfileCreationResponse orgResponse = new ProfileCreationResponse(
                UUID.randomUUID(),
                "testorg",
                "Test Organization",
                "org@example.com",
                "org-jwt-token",
                ProfileType.organization,
                Instant.now());

        when(authService.register(request)).thenReturn(orgResponse);

        // Act
        ResponseEntity<ProfileCreationResponse> response = authController.register(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(orgResponse, response.getBody());
        assertEquals(ProfileType.organization, response.getBody().type());
        verify(authService).register(request);
    }

    @Test
    void login_WithValidCredentials_ShouldReturnOkStatusWithPayload() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        UUID profileId = UUID.randomUUID();
        LoginResponse loginResponse = new LoginResponse(
                profileId,
                "login-jwt-token",
                "testuser",
                "Test User",
                "test@example.com",
                ProfileType.user);

        when(authService.login(request)).thenReturn(loginResponse);

        // Act
        ResponseEntity<LoginResponse> response = authController.login(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(loginResponse, response.getBody());
        assertEquals(profileId, response.getBody().id());
        assertEquals("testuser", response.getBody().username());
        verify(authService).login(request);
    }

    @Test
    void getPosts_ShouldReturnUserIdString() {
        // Arrange
        Profile mockProfile = new Profile();
        mockProfile.setId(UUID.randomUUID());
        mockProfile.setUsername("testuser");

        // Act
        String result = authController.getPosts(mockProfile);

        // Assert
        assertNotNull(result);
        assertEquals("userid: ", result);
    }

    @Test
    void register_ShouldCreateUriWithProfileId() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "test@example.com",
                "Test User",
                "testuser",
                "password123",
                ProfileType.user);

        when(authService.register(request)).thenReturn(mockProfileCreationResponse);

        // Act
        ResponseEntity<ProfileCreationResponse> response = authController.register(request);

        // Assert
        assertNotNull(response.getHeaders().getLocation());
        String locationPath = response.getHeaders().getLocation().getPath();
        assertTrue(locationPath.endsWith("/" + mockProfileCreationResponse.id()));
    }
}
