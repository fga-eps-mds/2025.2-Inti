package br.mds.inti.service;

import br.mds.inti.model.entity.Profile;
import br.mds.inti.model.enums.ProfileType;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;
    private Profile mockProfile;
    private String testSecret = "test-secret-key-for-jwt-signing";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", testSecret);

        mockProfile = new Profile();
        mockProfile.setId(UUID.randomUUID());
        mockProfile.setUsername("testuser");
        mockProfile.setName("Test User");
        mockProfile.setEmail("test@example.com");
        mockProfile.setType(ProfileType.user);
    }

    @Test
    void generateToken_ShouldReturnValidJwtToken() {
        // Act
        String token = jwtService.generateToken(mockProfile);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT tem 3 partes separadas por ponto
    }

    @Test
    void generateToken_ShouldContainEmailAsSubject() {
        // Act
        String token = jwtService.generateToken(mockProfile);

        // Assert
        String decodedEmail = JWT.decode(token).getSubject();
        assertEquals("test@example.com", decodedEmail);
    }

    @Test
    void generateToken_ShouldHaveExpirationDate() {
        // Act
        String token = jwtService.generateToken(mockProfile);

        // Assert
        assertNotNull(JWT.decode(token).getExpiresAt());
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnEmail() {
        // Arrange
        String token = jwtService.generateToken(mockProfile);

        // Act
        String email = jwtService.validateToken(token);

        // Assert
        assertNotNull(email);
        assertEquals("test@example.com", email);
    }

    @Test
    void validateToken_WithInvalidToken_ShouldThrowException() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> jwtService.validateToken(invalidToken));
        assertEquals("Token inválido ou expirado", exception.getMessage());
    }

    @Test
    void validateToken_WithTokenSignedByDifferentSecret_ShouldThrowException() {
        // Arrange
        String tokenWithDifferentSecret = JWT.create()
                .withSubject("test@example.com")
                .sign(Algorithm.HMAC256("different-secret"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> jwtService.validateToken(tokenWithDifferentSecret));
        assertEquals("Token inválido ou expirado", exception.getMessage());
    }

    @Test
    void generateToken_ForDifferentProfiles_ShouldGenerateDifferentTokens() {
        // Arrange
        Profile profile1 = new Profile();
        profile1.setEmail("user1@example.com");

        Profile profile2 = new Profile();
        profile2.setEmail("user2@example.com");

        // Act
        String token1 = jwtService.generateToken(profile1);
        String token2 = jwtService.generateToken(profile2);

        // Assert
        assertNotEquals(token1, token2);
    }

    @Test
    void validateToken_ShouldExtractCorrectEmailFromToken() {
        // Arrange
        Profile profileWithSpecificEmail = new Profile();
        profileWithSpecificEmail.setEmail("specificemail123@example.com");
        String token = jwtService.generateToken(profileWithSpecificEmail);

        // Act
        String extractedEmail = jwtService.validateToken(token);

        // Assert
        assertEquals("specificemail123@example.com", extractedEmail);
    }

    @Test
    void generateToken_WithNullEmail_ShouldThrowException() {
        // Arrange
        Profile profileWithNullEmail = new Profile();
        profileWithNullEmail.setEmail(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> jwtService.generateToken(profileWithNullEmail));
        assertEquals("Profile email must be defined to generate a JWT token", exception.getMessage());
    }

    @Test
    void generateToken_WithBlankEmail_ShouldThrowException() {
        // Arrange
        Profile profileWithBlankEmail = new Profile();
        profileWithBlankEmail.setEmail("   ");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> jwtService.generateToken(profileWithBlankEmail));
        assertEquals("Profile email must be defined to generate a JWT token", exception.getMessage());
    }

    @Test
    void validateToken_WithEmptyToken_ShouldThrowException() {
        // Arrange
        String emptyToken = "";

        // Act & Assert
        assertThrows(RuntimeException.class, () -> jwtService.validateToken(emptyToken));
    }

    @Test
    void validateToken_WithNullToken_ShouldThrowException() {
        // Arrange
        String nullToken = null;

        // Act & Assert
        assertThrows(RuntimeException.class, () -> jwtService.validateToken(nullToken));
    }
}
