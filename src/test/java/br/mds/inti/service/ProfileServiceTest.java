package br.mds.inti.service;

import br.mds.inti.model.dto.PostResponse;
import br.mds.inti.model.dto.ProfileResponse;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.model.enums.ProfileType;
import br.mds.inti.repositories.ProfileRepository;
import br.mds.inti.service.exceptions.ProfileNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private PostService postService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private ProfileService profileService;

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
        mockProfile.setPassword("password");
        mockProfile.setProfilePictureUrl("http://example.com/avatar.jpg");
        mockProfile.setBio("Test bio");
        mockProfile.setType(ProfileType.user);
        mockProfile.setFollowersCount(10);
        mockProfile.setFollowingCount(5);
        mockProfile.setCreatedAt(Instant.now());
    }

    @Test
    void getProfile_WhenAuthenticated_ShouldReturnProfileResponse() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockProfile);
        SecurityContextHolder.setContext(securityContext);

        // Cria PostResponse com o construtor CORRETO baseado no seu record
        UUID postId1 = UUID.randomUUID();
        UUID postId2 = UUID.randomUUID();

        Page<PostResponse> mockPosts = new PageImpl<>(List.of(
                new PostResponse(postId1, "http://example.com/img1.jpg", "Description 1", 10, "2024-01-01"),
                new PostResponse(postId2, "http://example.com/img2.jpg", "Description 2", 5, "2024-01-02")));

        when(postService.getPostByIdProfile(profileId, PageRequest.of(0, 10))).thenReturn(mockPosts);

        // Act
        ProfileResponse result = profileService.getProfile(0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(mockProfile.getName(), result.name());
        assertEquals(mockProfile.getUsername(), result.username());
        assertEquals(mockProfile.getProfilePictureUrl(), result.profile_picture_url());
        assertEquals(mockProfile.getBio(), result.bio());
        assertEquals(mockProfile.getFollowersCount(), result.followersCount());
        assertEquals(mockProfile.getFollowingCount(), result.followingCount());
        assertEquals(2, result.posts().size());
    }

    @Test
    void getProfile_WhenNotAuthenticated_ShouldThrowException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("anonymous");
        SecurityContextHolder.setContext(securityContext);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> profileService.getProfile(0, 10));
    }

    @Test
    void getProfileByUsername_WhenUserExists_ShouldReturnProfileResponse() {
        // Arrange
        String username = "testuser";
        when(profileRepository.findByUsername(username)).thenReturn(Optional.of(mockProfile));

        Page<PostResponse> mockPosts = new PageImpl<>(List.of());
        when(postService.getPostByIdProfile(profileId, PageRequest.of(0, 10))).thenReturn(mockPosts);

        // Act
        ProfileResponse result = profileService.getProfileByUsername(username, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(mockProfile.getName(), result.name());
        assertEquals(mockProfile.getUsername(), result.username());
        verify(profileRepository).findByUsername(username);
    }

    @Test
    void getProfileByUsername_WhenUserNotFound_ShouldThrowProfileNotFoundException() {
        // Arrange
        String username = "nonexistent";
        when(profileRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProfileNotFoundException.class, () -> profileService.getProfileByUsername(username, 0, 10));
    }

    @Test
    void getProfile_WhenUserExists_ShouldReturnProfile() {
        // Arrange
        String username = "testuser";
        when(profileRepository.findByUsername(username)).thenReturn(Optional.of(mockProfile));

        // Act
        Profile result = profileService.getProfile(username);

        // Assert
        assertNotNull(result);
        assertEquals(mockProfile, result);
        verify(profileRepository).findByUsername(username);
    }

    @Test
    void getProfile_WhenUserNotFound_ShouldThrowProfileNotFoundException() {
        // Arrange
        String username = "nonexistent";
        when(profileRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProfileNotFoundException.class, () -> profileService.getProfile(username));
    }

    @Test
    void incrementFollowingCount_ShouldIncreaseCountAndSave() {
        // Arrange
        int initialCount = mockProfile.getFollowingCount();
        when(profileRepository.save(mockProfile)).thenReturn(mockProfile);

        // Act
        profileService.incrementFollowingCount(mockProfile);

        // Assert
        assertEquals(initialCount + 1, mockProfile.getFollowingCount());
        verify(profileRepository).save(mockProfile);
    }

    @Test
    void incrementFollowerCount_ShouldIncreaseCountAndSave() {
        // Arrange
        int initialCount = mockProfile.getFollowersCount();
        when(profileRepository.save(mockProfile)).thenReturn(mockProfile);

        // Act
        profileService.incrementFollowerCount(mockProfile);

        // Assert
        assertEquals(initialCount + 1, mockProfile.getFollowersCount());
        verify(profileRepository).save(mockProfile);
    }

    @Test
    void decrementFollowingCount_ShouldDecreaseCountAndSave() {
        // Arrange
        int initialCount = mockProfile.getFollowingCount();
        when(profileRepository.save(mockProfile)).thenReturn(mockProfile);

        // Act
        profileService.decrementFollowingCount(mockProfile);

        // Assert
        assertEquals(initialCount - 1, mockProfile.getFollowingCount());
        verify(profileRepository).save(mockProfile);
    }

    @Test
    void decrementFollowerCount_ShouldDecreaseCountAndSave() {
        // Arrange
        int initialCount = mockProfile.getFollowersCount();
        when(profileRepository.save(mockProfile)).thenReturn(mockProfile);

        // Act
        profileService.decrementFollowerCount(mockProfile);

        // Assert
        assertEquals(initialCount - 1, mockProfile.getFollowersCount());
        verify(profileRepository).save(mockProfile);
    }
}