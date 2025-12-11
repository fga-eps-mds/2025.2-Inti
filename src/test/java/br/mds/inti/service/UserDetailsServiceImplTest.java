package br.mds.inti.service;

import br.mds.inti.model.entity.Profile;
import br.mds.inti.model.enums.ProfileType;
import br.mds.inti.repositories.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private Profile existingProfile;

    @BeforeEach
    void setUp() {
        existingProfile = new Profile();
        existingProfile.setId(UUID.randomUUID());
        existingProfile.setEmail("existing@example.com");
        existingProfile.setUsername("existinguser");
        existingProfile.setName("Existing User");
        existingProfile.setType(ProfileType.user);
        existingProfile.setCreatedAt(Instant.now());
    }

    @Test
    void loadUserByUsername_WithExistingEmail_ShouldReturnUserDetails() {
        // Arrange
        when(profileRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingProfile));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("existing@example.com");

        // Assert
        assertNotNull(userDetails);
        assertEquals("existinguser", userDetails.getUsername());
        assertEquals(existingProfile.getPassword(), userDetails.getPassword());
    }

    @Test
    void loadUserByUsername_WithUnknownEmail_ShouldThrowException() {
        // Arrange
        when(profileRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("unknown@example.com"));
        assertTrue(exception.getMessage().contains("unknown@example.com"));
    }
}
