package br.mds.inti.service;

import br.mds.inti.model.dto.FollowResponse;
import br.mds.inti.model.entity.Follow;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.model.entity.pk.FollowsPK;
import br.mds.inti.model.enums.ProfileType;
import br.mds.inti.repositories.FollowRepository;
import br.mds.inti.service.exceptions.ProfileNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private ProfileService profileService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private FollowService followService;

    private Profile mockProfileMe;
    private Profile mockProfileToFollow;
    private FollowsPK mockFollowsPK;
    private Follow mockFollow;

    @BeforeEach
    void setUp() {
        UUID profileIdMe = UUID.randomUUID();
        UUID profileIdToFollow = UUID.randomUUID();

        mockProfileMe = new Profile();
        mockProfileMe.setId(profileIdMe);
        mockProfileMe.setUsername("me");
        mockProfileMe.setName("My Profile");
        mockProfileMe.setEmail("me@example.com");
        mockProfileMe.setPassword("password");
        mockProfileMe.setFollowersCount(10);
        mockProfileMe.setFollowingCount(5);
        mockProfileMe.setType(ProfileType.user);
        mockProfileMe.setCreatedAt(Instant.now());

        mockProfileToFollow = new Profile();
        mockProfileToFollow.setId(profileIdToFollow);
        mockProfileToFollow.setUsername("usertofollow");
        mockProfileToFollow.setName("User To Follow");
        mockProfileToFollow.setEmail("follow@example.com");
        mockProfileToFollow.setPassword("password");
        mockProfileToFollow.setFollowersCount(20);
        mockProfileToFollow.setFollowingCount(15);
        mockProfileToFollow.setType(ProfileType.user);
        mockProfileToFollow.setCreatedAt(Instant.now());

        mockFollowsPK = new FollowsPK();
        mockFollowsPK.setFollowerProfileId(mockProfileToFollow.getId());
        mockFollowsPK.setFollowingProfileId(mockProfileMe.getId());

        mockFollow = new Follow();
        mockFollow.setId(mockFollowsPK);
        mockFollow.setFollowerProfile(mockProfileToFollow);
        mockFollow.setFollowingProfile(mockProfileMe);
        mockFollow.setCreatedAt(Instant.now());
    }

    @Test
    void followProfile_WhenAuthenticatedAndProfileExists_ShouldFollowSuccessfully() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockProfileMe);
        SecurityContextHolder.setContext(securityContext);

        when(profileService.getProfile("usertofollow")).thenReturn(mockProfileToFollow);
        when(followRepository.save(any(Follow.class))).thenReturn(mockFollow);

        // Act
        FollowResponse result = followService.followProfile("usertofollow");

        // Assert
        assertNotNull(result);
        assertEquals("Perfil seguido com sucesso.", result.message());

        verify(profileService).incrementFollowerCount(mockProfileToFollow);
        verify(profileService).incrementFollowingCount(mockProfileMe);
        verify(followRepository).save(any(Follow.class));
    }

    @Test
    void followProfile_WhenNotAuthenticated_ShouldThrowException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("anonymous");
        SecurityContextHolder.setContext(securityContext);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> followService.followProfile("usertofollow"));
    }

    @Test
    void followProfile_WhenProfileToFollowNotFound_ShouldThrowProfileNotFoundException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockProfileMe);
        SecurityContextHolder.setContext(securityContext);

        when(profileService.getProfile("nonexistent")).thenThrow(new ProfileNotFoundException("nonexistent"));

        // Act & Assert
        assertThrows(ProfileNotFoundException.class, () -> followService.followProfile("nonexistent"));
    }

    @Test
    void followProfile_WhenProfileToFollowIsNull_ShouldThrowProfileNotFoundException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockProfileMe);
        SecurityContextHolder.setContext(securityContext);

        when(profileService.getProfile("usertofollow")).thenReturn(null);

        // Act & Assert
        assertThrows(ProfileNotFoundException.class, () -> followService.followProfile("usertofollow"));
    }

    @Test
    void unfollowProfile_WhenAuthenticatedAndFollowExists_ShouldUnfollowSuccessfully() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockProfileMe);
        SecurityContextHolder.setContext(securityContext);

        when(profileService.getProfile("usertofollow")).thenReturn(mockProfileToFollow);
        when(followRepository.findFollowRelationship(mockProfileToFollow, mockProfileMe))
                .thenReturn(Optional.of(mockFollow));

        // Act
        FollowResponse result = followService.unfollowProfile("usertofollow");

        // Assert
        assertNotNull(result);
        assertEquals("Você deixou de seguir este perfil.", result.message());

        verify(profileService).decrementFollowingCount(mockProfileMe);
        verify(profileService).decrementFollowerCount(mockProfileToFollow);
        verify(followRepository).delete(mockFollow);
    }

    @Test
    void unfollowProfile_WhenNotAuthenticated_ShouldThrowException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("anonymous");
        SecurityContextHolder.setContext(securityContext);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> followService.unfollowProfile("usertofollow"));
    }

    @Test
    void unfollowProfile_WhenFollowNotFound_ShouldThrowException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockProfileMe);
        SecurityContextHolder.setContext(securityContext);

        when(profileService.getProfile("usertofollow")).thenReturn(mockProfileToFollow);
        when(followRepository.findFollowRelationship(mockProfileToFollow, mockProfileMe))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> followService.unfollowProfile("usertofollow"));
        assertEquals("Follow não encontrado", exception.getMessage());
    }

    @Test
    void followProfile_ShouldCreateCorrectFollowsPK() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockProfileMe);
        SecurityContextHolder.setContext(securityContext);

        when(profileService.getProfile("usertofollow")).thenReturn(mockProfileToFollow);
        when(followRepository.save(any(Follow.class))).thenAnswer(invocation -> {
            Follow savedFollow = invocation.getArgument(0);
            // Verify that the FollowsPK is created correctly
            assertEquals(mockProfileToFollow.getId(), savedFollow.getId().getFollowerProfileId());
            assertEquals(mockProfileMe.getId(), savedFollow.getId().getFollowingProfileId());
            assertEquals(mockProfileToFollow, savedFollow.getFollowerProfile());
            assertEquals(mockProfileMe, savedFollow.getFollowingProfile());
            assertNotNull(savedFollow.getCreatedAt());
            return savedFollow;
        });

        // Act
        FollowResponse result = followService.followProfile("usertofollow");

        // Assert
        assertNotNull(result);
        verify(followRepository).save(any(Follow.class));
    }

    @Test
    void unfollowProfile_WhenProfileToUnfollowNotFound_ShouldThrowProfileNotFoundException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockProfileMe);
        SecurityContextHolder.setContext(securityContext);

        when(profileService.getProfile("nonexistent")).thenThrow(new ProfileNotFoundException("nonexistent"));

        // Act & Assert
        assertThrows(ProfileNotFoundException.class, () -> followService.unfollowProfile("nonexistent"));
    }
}