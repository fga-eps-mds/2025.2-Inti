package br.mds.inti.controller;

import br.mds.inti.model.entity.Post;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.model.enums.PostType;
import br.mds.inti.model.enums.ProfileType;
import br.mds.inti.service.FeedService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedControllerTest {

    @Mock
    private FeedService feedService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private FeedController feedController;

    private Profile currentProfile;
    private UUID currentProfileId;

    @BeforeEach
    void setUp() {
        currentProfileId = UUID.randomUUID();
        currentProfile = new Profile();
        currentProfile.setId(currentProfileId);
        currentProfile.setUsername("testuser");
        currentProfile.setType(ProfileType.user);
    }

    // ========== getOrganizationDashboard Tests ==========

    @Test
    void getOrganizationDashboard_ShouldReturnWelcomeMessage() {
        // Act
        ResponseEntity<String> response = feedController.getOrganizationDashboard();

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals("Bem-vindo à área exclusiva de organizações!", response.getBody());
    }

    @Test
    void getOrganizationDashboard_ShouldReturnStringType() {
        // Act
        ResponseEntity<String> response = feedController.getOrganizationDashboard();

        // Assert
        assertNotNull(response.getBody());
        assertInstanceOf(String.class, response.getBody());
    }

    @Test
    void getOrganizationDashboard_ShouldNotReturnEmptyString() {
        // Act
        ResponseEntity<String> response = feedController.getOrganizationDashboard();

        // Assert
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }

    // ========== getFeedWithMetadata Tests ==========

    @Test
    void getFeedWithMetadata_withValidAuth_shouldReturnFeedItems() {
        // Arrange
        Post post = createPost();
        FeedService.ClassifiedPost classifiedPost = new FeedService.ClassifiedPost(
                post, PostType.FOLLOWED, "Perfil seguido / próprio", true);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(currentProfile);
        when(feedService.generateFeed(eq(currentProfile), eq(0), eq(20)))
                .thenReturn(List.of(classifiedPost));

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            ResponseEntity<List<FeedController.FeedItem>> response = feedController.getFeedWithMetadata(0, 20);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(1);

            FeedController.FeedItem item = response.getBody().get(0);
            assertThat(item.id()).isEqualTo(post.getId());
            assertThat(item.username()).isEqualTo(post.getProfile().getUsername());
            assertThat(item.description()).isEqualTo(post.getDescription());
            assertThat(item.type()).isEqualTo(PostType.FOLLOWED);
            assertThat(item.reason()).isEqualTo("Perfil seguido / próprio");
            assertThat(item.liked()).isTrue();
        }
    }

    @Test
    void getFeedWithMetadata_withNoAuth_shouldReturn401() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            ResponseEntity<List<FeedController.FeedItem>> response = feedController.getFeedWithMetadata(0, 20);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).isNull();
        }
    }

    @Test
    void getFeedWithMetadata_withInvalidPrincipal_shouldReturn401() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("not-a-profile");

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            ResponseEntity<List<FeedController.FeedItem>> response = feedController.getFeedWithMetadata(0, 20);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Test
    void getFeedWithMetadata_shouldMapImageUrlsCorrectly() {
        // Arrange
        Post post = createPost();
        post.setBlobName("test-image.jpg");
        post.getProfile().setProfilePictureUrl("profile-pic.png");

        FeedService.ClassifiedPost classifiedPost = new FeedService.ClassifiedPost(
                post, PostType.RANDOM, "Descoberta", false);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(currentProfile);
        when(feedService.generateFeed(any(), eq(0), eq(20)))
                .thenReturn(List.of(classifiedPost));

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            ResponseEntity<List<FeedController.FeedItem>> response = feedController.getFeedWithMetadata(0, 20);

            // Assert
            assertThat(response.getBody()).isNotNull();
            FeedController.FeedItem item = response.getBody().get(0);
            assertThat(item.imageUrl()).isEqualTo("/images/test-image.jpg");
            assertThat(item.imageProfileUrl()).isEqualTo("/images/profile-pic.png");
        }
    }

    @Test
    void getFeedWithMetadata_withNullBlobName_shouldReturnNullImageUrl() {
        // Arrange
        Post post = createPost();
        post.setBlobName(null);
        post.getProfile().setProfilePictureUrl(null);

        FeedService.ClassifiedPost classifiedPost = new FeedService.ClassifiedPost(
                post, PostType.RANDOM, "Descoberta", false);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(currentProfile);
        when(feedService.generateFeed(any(), eq(0), eq(20)))
                .thenReturn(List.of(classifiedPost));

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            ResponseEntity<List<FeedController.FeedItem>> response = feedController.getFeedWithMetadata(0, 20);

            // Assert
            assertThat(response.getBody()).isNotNull();
            FeedController.FeedItem item = response.getBody().get(0);
            assertThat(item.imageUrl()).isNull();
            assertThat(item.imageProfileUrl()).isNull();
        }
    }

    @Test
    void getFeedWithMetadata_shouldPassPaginationParams() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(currentProfile);
        when(feedService.generateFeed(eq(currentProfile), eq(2), eq(15)))
                .thenReturn(List.of());

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            feedController.getFeedWithMetadata(2, 15);

            // Assert
            verify(feedService).generateFeed(currentProfile, 2, 15);
        }
    }

    @Test
    void getFeedWithMetadata_shouldReturnEmptyListWhenNoFeed() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(currentProfile);
        when(feedService.generateFeed(any(), eq(0), eq(20)))
                .thenReturn(List.of());

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            ResponseEntity<List<FeedController.FeedItem>> response = feedController.getFeedWithMetadata(0, 20);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    @Test
    void getFeedWithMetadata_shouldMapAllPostTypes() {
        // Arrange
        Post followedPost = createPostWithType("followed", PostType.FOLLOWED);
        Post orgPost = createPostWithType("org", PostType.ORGANIZATION);
        Post randomPost = createPostWithType("random", PostType.RANDOM);
        Post popularPost = createPostWithType("popular", PostType.POPULAR);
        Post secondDegreePost = createPostWithType("seconddegree", PostType.SECOND_DEGREE);

        List<FeedService.ClassifiedPost> classifiedPosts = List.of(
                new FeedService.ClassifiedPost(followedPost, PostType.FOLLOWED, "Perfil seguido / próprio", true),
                new FeedService.ClassifiedPost(orgPost, PostType.ORGANIZATION, "Post de organização", false),
                new FeedService.ClassifiedPost(randomPost, PostType.RANDOM, "Descoberta", false),
                new FeedService.ClassifiedPost(popularPost, PostType.POPULAR, "Post popular", false),
                new FeedService.ClassifiedPost(secondDegreePost, PostType.SECOND_DEGREE, "Conexão de segundo grau",
                        false));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(currentProfile);
        when(feedService.generateFeed(any(), eq(0), eq(20)))
                .thenReturn(classifiedPosts);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act
            ResponseEntity<List<FeedController.FeedItem>> response = feedController.getFeedWithMetadata(0, 20);

            // Assert
            assertThat(response.getBody()).hasSize(5);
            assertThat(response.getBody().stream().map(FeedController.FeedItem::type))
                    .containsExactlyInAnyOrder(
                            PostType.FOLLOWED,
                            PostType.ORGANIZATION,
                            PostType.RANDOM,
                            PostType.POPULAR,
                            PostType.SECOND_DEGREE);
        }
    }

    // ========== Helper Methods ==========

    private Post createPost() {
        Profile profile = new Profile();
        profile.setId(UUID.randomUUID());
        profile.setUsername("postauthor");
        profile.setType(ProfileType.user);

        Post post = new Post();
        post.setId(UUID.randomUUID());
        post.setProfile(profile);
        post.setDescription("Test description");
        post.setBlobName("test-blob.jpg");
        post.setLikesCount(5);
        post.setCreatedAt(Instant.now());
        return post;
    }

    private Post createPostWithType(String username, PostType type) {
        Profile profile = new Profile();
        profile.setId(UUID.randomUUID());
        profile.setUsername(username);
        profile.setType(type == PostType.ORGANIZATION ? ProfileType.organization : ProfileType.user);

        Post post = new Post();
        post.setId(UUID.randomUUID());
        post.setProfile(profile);
        post.setDescription("Description for " + username);
        post.setBlobName(username + "-blob.jpg");
        post.setLikesCount(type == PostType.POPULAR ? 15 : 0);
        post.setCreatedAt(Instant.now());
        return post;
    }
}
