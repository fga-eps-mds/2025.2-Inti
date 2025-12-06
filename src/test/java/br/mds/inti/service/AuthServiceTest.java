package br.mds.inti.service;

import br.mds.inti.model.dto.auth.LoginRequest;
import br.mds.inti.model.dto.auth.ProfileCreationResponse;
import br.mds.inti.model.dto.auth.RegisterRequest;
import br.mds.inti.model.dto.auth.LoginResponse;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.model.enums.ProfileType;
import br.mds.inti.repositories.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private Profile mockProfile;
    private UUID profileId;

    @BeforeEach
    void setUp() {
        profileId = UUID.randomUUID();
        mockProfile = new Profile();
        mockProfile.setId(profileId);
        mockProfile.setUsername("testuser");
        mockProfile.setName("Test User");
        mockProfile.setEmail("test@example.com");
        mockProfile.setPassword("encodedPassword");
        mockProfile.setType(ProfileType.user);
        mockProfile.setFollowersCount(0);
        mockProfile.setFollowingCount(0);
        mockProfile.setCreatedAt(Instant.now());
    }

    @Test
    void register_WithValidRequest_ShouldCreateUserAndReturnResponse() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "test@example.com",
                "Test User",
                "testuser",
                "password123",
                ProfileType.user);

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(profileRepository.save(any(Profile.class))).thenReturn(mockProfile);
        when(jwtService.generateToken(any(Profile.class))).thenReturn("generated-jwt-token");

        // Act
        ProfileCreationResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals(profileId, response.id());
        assertEquals("testuser", response.username());
        assertEquals("Test User", response.name());
        assertEquals("test@example.com", response.email());
        assertEquals("generated-jwt-token", response.jwt());
        assertEquals(ProfileType.user, response.type());
        assertNotNull(response.createdAt());

        verify(passwordEncoder).encode("password123");
        verify(profileRepository).save(any(Profile.class));
        verify(jwtService).generateToken(any(Profile.class));
    }

    @Test
    void register_WithNullType_ShouldDefaultToUserType() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "test@example.com",
                "Test User",
                "testuser",
                "password123",
                null);

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> {
            Profile savedProfile = invocation.getArgument(0);
            assertEquals(ProfileType.user, savedProfile.getType());
            return mockProfile;
        });
        when(jwtService.generateToken(any(Profile.class))).thenReturn("generated-jwt-token");

        // Act
        ProfileCreationResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void register_WithOrganizationType_ShouldCreateOrganizationProfile() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "org@example.com",
                "Test Organization",
                "testorg",
                "password123",
                ProfileType.organization);

        Profile orgProfile = new Profile();
        orgProfile.setId(UUID.randomUUID());
        orgProfile.setUsername("testorg");
        orgProfile.setName("Test Organization");
        orgProfile.setEmail("org@example.com");
        orgProfile.setType(ProfileType.organization);
        orgProfile.setCreatedAt(Instant.now());

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(profileRepository.save(any(Profile.class))).thenReturn(orgProfile);
        when(jwtService.generateToken(any(Profile.class))).thenReturn("org-jwt-token");

        // Act
        ProfileCreationResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals(ProfileType.organization, response.type());
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void login_WithValidCredentials_ShouldReturnLoginPayload() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "password123");

        when(profileRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockProfile));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(mockProfile)).thenReturn("login-jwt-token");

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("login-jwt-token", response.jwt());
        assertEquals("testuser", response.username());
        assertEquals("Test User", response.name());
        assertEquals("test@example.com", response.email());
        assertEquals(ProfileType.user, response.type());
        verify(profileRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtService).generateToken(mockProfile);
    }

    @Test
    void login_WithNonExistentEmail_ShouldThrowException() {
        // Arrange
        LoginRequest request = new LoginRequest("nonexistent@example.com", "password123");

        when(profileRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(profileRepository).findByEmail("nonexistent@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_WithInvalidPassword_ShouldThrowException() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");

        when(profileRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockProfile));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("Usuario ou senha invalidos", exception.getMessage());
        verify(profileRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("wrongpassword", "encodedPassword");
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void register_ShouldSetFollowerAndFollowingCountsToZero() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "test@example.com",
                "Test User",
                "testuser",
                "password123",
                ProfileType.user);

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> {
            Profile savedProfile = invocation.getArgument(0);
            assertEquals(0, savedProfile.getFollowersCount());
            assertEquals(0, savedProfile.getFollowingCount());
            return mockProfile;
        });
        when(jwtService.generateToken(any(Profile.class))).thenReturn("generated-jwt-token");

        // Act
        authService.register(request);

        // Assert
        verify(profileRepository).save(any(Profile.class));
    }
}
