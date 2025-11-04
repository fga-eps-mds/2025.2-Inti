package br.mds.inti.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

import br.mds.inti.model.dto.PostResponse;
import br.mds.inti.model.dto.ProfileResponse;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.repositories.ProfileRepository;
import br.mds.inti.service.exceptions.ProfileNotFoundException;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceTest {

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

    private Profile testProfile;
    private List<PostResponse> postList;
    private Page<PostResponse> postPage;

    @BeforeEach
    void setUp() {
        testProfile = new Profile();
        testProfile.setId(UUID.randomUUID());
        testProfile.setName("Test User");
        testProfile.setUsername("testuser");
        testProfile.setProfilePictureUrl("http://example.com/pic.jpg");
        testProfile.setBio("Test bio");
        testProfile.setFollowersCount(10);
        testProfile.setFollowingCount(20);

        postList = new ArrayList<>();
        postPage = new PageImpl<>(postList);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getProfile_WhenAuthenticated_ReturnsProfileResponse() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testProfile);
        when(postService.getPostByIdProfile(any(UUID.class), any(PageRequest.class)))
                .thenReturn(postPage);

        // Act
        ProfileResponse response = profileService.getProfile(0, 10);

        // Assert
        assertNotNull(response);
        assertEquals(testProfile.getName(), response.name());
        assertEquals(testProfile.getUsername(), response.username());
        assertEquals(testProfile.getProfilePictureUrl(), response.profile_picture_url());
        assertEquals(testProfile.getBio(), response.bio());
        assertEquals(testProfile.getFollowersCount(), response.followersCount());
        assertEquals(testProfile.getFollowingCount(), response.followingCount());
        assertEquals(postList, response.posts());

        verify(postService).getPostByIdProfile(testProfile.getId(), PageRequest.of(0, 10));
    }

    @Test
    void getProfile_WhenNotAuthenticated_ThrowsException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> profileService.getProfile(0, 10));
        assertEquals("profile nao autenticado", exception.getMessage());
    }

    @Test
    void getProfileByUsername_WhenProfileExists_ReturnsProfileResponse() {
        // Arrange
        when(profileRepository.findByUsername(anyString())).thenReturn(Optional.of(testProfile));
        when(postService.getPostByIdProfile(any(UUID.class), any(PageRequest.class)))
                .thenReturn(postPage);

        // Act
        ProfileResponse response = profileService.getProfileByUsername("testuser", 0, 10);

        // Assert
        assertNotNull(response);
        assertEquals(testProfile.getName(), response.name());
        assertEquals(testProfile.getUsername(), response.username());
        assertEquals(testProfile.getProfilePictureUrl(), response.profile_picture_url());
        assertEquals(testProfile.getBio(), response.bio());
        assertEquals(testProfile.getFollowersCount(), response.followersCount());
        assertEquals(testProfile.getFollowingCount(), response.followingCount());
        assertEquals(postList, response.posts());

        verify(profileRepository).findByUsername("testuser");
        verify(postService).getPostByIdProfile(testProfile.getId(), PageRequest.of(0, 10));
    }

    @Test
    void getProfileByUsername_WhenProfileNotFound_ThrowsProfileNotFoundException() {
        // Arrange
        String username = "nonexistent";
        when(profileRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(ProfileNotFoundException.class,
                () -> profileService.getProfileByUsername(username, 0, 10));
        assertEquals(username, exception.getMessage());

        verify(profileRepository).findByUsername(username);
        verify(postService, never()).getPostByIdProfile(any(), any());
    }
}