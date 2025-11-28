package br.mds.inti.service;

import br.mds.inti.model.entity.Post;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.model.enums.PostType;
import br.mds.inti.model.enums.ProfileType;
import br.mds.inti.repositories.FollowRepository;
import br.mds.inti.repositories.PostRepository;
import br.mds.inti.repositories.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private FeedService feedService;

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

    @Test
    void generateFeed_withEmptyFollowList_shouldReturnPosts() {
        // Arrange
        when(followRepository.findFollowedUserIds(currentProfileId)).thenReturn(Collections.emptyList());
        when(followRepository.findSecondDegreeConnectionIds(currentProfileId)).thenReturn(Collections.emptyList());
        when(followRepository.findFollowerIds(currentProfileId)).thenReturn(Collections.emptyList());
        when(profileRepository.findByOrganization(eq(ProfileType.organization), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
        when(postRepository.findPostByOrganizationAndNotDeleted(any(), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
        when(postRepository.findRecentPostsExcludingUsers(any(), any(PageRequest.class)))
                .thenReturn(createRandomPosts(5));

        // Act
        List<FeedService.ClassifiedPost> result = feedService.generateFeed(currentProfile, 0, 10);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSizeLessThanOrEqualTo(10);
    }

    @Test
    void generateFeed_shouldClassifyFollowedPosts() {
        // Arrange
        UUID followedUserId = UUID.randomUUID();
        Profile followedProfile = createProfile(followedUserId, "followeduser", ProfileType.user);
        Post followedPost = createPost(followedProfile, 0);

        when(followRepository.findFollowedUserIds(currentProfileId)).thenReturn(List.of(followedUserId));
        when(followRepository.findSecondDegreeConnectionIds(currentProfileId)).thenReturn(Collections.emptyList());
        when(followRepository.findFollowerIds(currentProfileId)).thenReturn(Collections.emptyList());
        when(postRepository.findByUserIdsAndNotDeleted(eq(List.of(followedUserId)), any(PageRequest.class)))
                .thenReturn(List.of(followedPost));
        when(profileRepository.findByOrganization(eq(ProfileType.organization), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
        when(postRepository.findPostByOrganizationAndNotDeleted(any(), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
        when(postRepository.findRecentPostsExcludingUsers(any(), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());

        // Act
        List<FeedService.ClassifiedPost> result = feedService.generateFeed(currentProfile, 0, 10);

        // Assert
        assertThat(result).isNotEmpty();
        FeedService.ClassifiedPost classifiedPost = result.stream()
                .filter(cp -> cp.post().getId().equals(followedPost.getId()))
                .findFirst()
                .orElse(null);
        assertThat(classifiedPost).isNotNull();
        assertThat(classifiedPost.type()).isEqualTo(PostType.FOLLOWED);
        assertThat(classifiedPost.reason()).isEqualTo("Perfil seguido / próprio");
    }

    @Test
    void generateFeed_shouldClassifyOrganizationPosts() {
        // Arrange
        UUID orgId = UUID.randomUUID();
        Profile orgProfile = createProfile(orgId, "orguser", ProfileType.organization);
        Post orgPost = createPost(orgProfile, 0);

        when(followRepository.findFollowedUserIds(currentProfileId)).thenReturn(Collections.emptyList());
        when(followRepository.findSecondDegreeConnectionIds(currentProfileId)).thenReturn(Collections.emptyList());
        when(followRepository.findFollowerIds(currentProfileId)).thenReturn(Collections.emptyList());
        when(profileRepository.findByOrganization(eq(ProfileType.organization), any(PageRequest.class)))
                .thenReturn(List.of(orgId));
        when(postRepository.findPostByOrganizationAndNotDeleted(eq(List.of(orgId)), any(PageRequest.class)))
                .thenReturn(List.of(orgPost));
        when(postRepository.findRecentPostsExcludingUsers(any(), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());

        // Act
        List<FeedService.ClassifiedPost> result = feedService.generateFeed(currentProfile, 0, 10);

        // Assert
        assertThat(result).isNotEmpty();
        FeedService.ClassifiedPost classifiedPost = result.stream()
                .filter(cp -> cp.post().getId().equals(orgPost.getId()))
                .findFirst()
                .orElse(null);
        assertThat(classifiedPost).isNotNull();
        assertThat(classifiedPost.type()).isEqualTo(PostType.ORGANIZATION);
        assertThat(classifiedPost.reason()).isEqualTo("Post de organização");
    }

    @Test
    void generateFeed_shouldClassifySecondDegreePosts() {
        // Arrange
        UUID secondDegreeUserId = UUID.randomUUID();
        Profile secondDegreeProfile = createProfile(secondDegreeUserId, "seconddegree", ProfileType.user);
        Post secondDegreePost = createPost(secondDegreeProfile, 0);

        UUID followerId = UUID.randomUUID();

        when(followRepository.findFollowedUserIds(currentProfileId)).thenReturn(Collections.emptyList());
        when(followRepository.findSecondDegreeConnectionIds(currentProfileId)).thenReturn(List.of(secondDegreeUserId));
        when(followRepository.findFollowerIds(currentProfileId)).thenReturn(List.of(followerId));
        when(followRepository.findFollowedByUsers(List.of(followerId))).thenReturn(List.of(secondDegreeUserId));
        when(postRepository.findByUserIdsAndNotDeleted(eq(List.of(secondDegreeUserId)), any(PageRequest.class)))
                .thenReturn(List.of(secondDegreePost));
        when(profileRepository.findByOrganization(eq(ProfileType.organization), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
        when(postRepository.findPostByOrganizationAndNotDeleted(any(), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
        when(postRepository.findRecentPostsExcludingUsers(any(), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());

        // Act
        List<FeedService.ClassifiedPost> result = feedService.generateFeed(currentProfile, 0, 10);

        // Assert
        assertThat(result).isNotEmpty();
        FeedService.ClassifiedPost classifiedPost = result.stream()
                .filter(cp -> cp.post().getId().equals(secondDegreePost.getId()))
                .findFirst()
                .orElse(null);
        assertThat(classifiedPost).isNotNull();
        assertThat(classifiedPost.type()).isEqualTo(PostType.SECOND_DEGREE);
        assertThat(classifiedPost.reason()).isEqualTo("Conexão de segundo grau");
    }

    @Test
    void generateFeed_shouldClassifyRandomPosts() {
        // Arrange
        UUID randomUserId = UUID.randomUUID();
        Profile randomProfile = createProfile(randomUserId, "randomuser", ProfileType.user);
        Post randomPost = createPost(randomProfile, 0);

        when(followRepository.findFollowedUserIds(currentProfileId)).thenReturn(Collections.emptyList());
        when(followRepository.findSecondDegreeConnectionIds(currentProfileId)).thenReturn(Collections.emptyList());
        when(followRepository.findFollowerIds(currentProfileId)).thenReturn(Collections.emptyList());
        when(profileRepository.findByOrganization(eq(ProfileType.organization), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
        when(postRepository.findPostByOrganizationAndNotDeleted(any(), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
        when(postRepository.findRecentPostsExcludingUsers(any(), any(PageRequest.class)))
                .thenReturn(List.of(randomPost));

        // Act
        List<FeedService.ClassifiedPost> result = feedService.generateFeed(currentProfile, 0, 10);

        // Assert
        assertThat(result).isNotEmpty();
        FeedService.ClassifiedPost classifiedPost = result.stream()
                .filter(cp -> cp.post().getId().equals(randomPost.getId()))
                .findFirst()
                .orElse(null);
        assertThat(classifiedPost).isNotNull();
        assertThat(classifiedPost.type()).isEqualTo(PostType.RANDOM);
        assertThat(classifiedPost.reason()).isEqualTo("Descoberta");
    }

    @Test
    void generateFeed_shouldClassifyOwnPostsAsFollowed() {
        // Arrange
        Post ownPost = createPost(currentProfile, 0);

        when(followRepository.findFollowedUserIds(currentProfileId)).thenReturn(Collections.emptyList());
        when(followRepository.findSecondDegreeConnectionIds(currentProfileId)).thenReturn(Collections.emptyList());
        when(followRepository.findFollowerIds(currentProfileId)).thenReturn(Collections.emptyList());
        when(profileRepository.findByOrganization(eq(ProfileType.organization), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
        when(postRepository.findPostByOrganizationAndNotDeleted(any(), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
        when(postRepository.findRecentPostsExcludingUsers(any(), any(PageRequest.class)))
                .thenReturn(List.of(ownPost));

        // Act
        List<FeedService.ClassifiedPost> result = feedService.generateFeed(currentProfile, 0, 10);

        // Assert
        assertThat(result).isNotEmpty();
        FeedService.ClassifiedPost classifiedPost = result.stream()
                .filter(cp -> cp.post().getId().equals(ownPost.getId()))
                .findFirst()
                .orElse(null);
        assertThat(classifiedPost).isNotNull();
        assertThat(classifiedPost.type()).isEqualTo(PostType.FOLLOWED);
    }

    @Test
    void generateFeed_shouldClassifyPopularPosts() {
        // Arrange
        UUID randomUserId = UUID.randomUUID();
        Profile randomProfile = createProfile(randomUserId, "popularuser", ProfileType.user);
        Post popularPost = createPost(randomProfile, 15); // > 10 likes = popular

        when(followRepository.findFollowedUserIds(currentProfileId)).thenReturn(Collections.emptyList());
        when(followRepository.findSecondDegreeConnectionIds(currentProfileId)).thenReturn(Collections.emptyList());
        when(followRepository.findFollowerIds(currentProfileId)).thenReturn(Collections.emptyList());
        when(profileRepository.findByOrganization(eq(ProfileType.organization), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
        when(postRepository.findPostByOrganizationAndNotDeleted(any(), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
        when(postRepository.findRecentPostsExcludingUsers(any(), any(PageRequest.class)))
                .thenReturn(List.of(popularPost));

        // Act
        List<FeedService.ClassifiedPost> result = feedService.generateFeed(currentProfile, 0, 10);

        // Assert
        assertThat(result).isNotEmpty();
        FeedService.ClassifiedPost classifiedPost = result.stream()
                .filter(cp -> cp.post().getId().equals(popularPost.getId()))
                .findFirst()
                .orElse(null);
        assertThat(classifiedPost).isNotNull();
        assertThat(classifiedPost.type()).isEqualTo(PostType.POPULAR);
        assertThat(classifiedPost.reason()).isEqualTo("Post popular");
    }

    @Test
    void generateFeed_pagination_shouldReturnCorrectPageSize() {
        // Arrange
        List<Post> manyPosts = createRandomPosts(30);

        when(followRepository.findFollowedUserIds(currentProfileId)).thenReturn(Collections.emptyList());
        when(followRepository.findSecondDegreeConnectionIds(currentProfileId)).thenReturn(Collections.emptyList());
        when(followRepository.findFollowerIds(currentProfileId)).thenReturn(Collections.emptyList());
        when(profileRepository.findByOrganization(eq(ProfileType.organization), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
        when(postRepository.findPostByOrganizationAndNotDeleted(any(), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
        when(postRepository.findRecentPostsExcludingUsers(any(), any(PageRequest.class)))
                .thenReturn(manyPosts);

        // Act
        List<FeedService.ClassifiedPost> page0 = feedService.generateFeed(currentProfile, 0, 10);
        List<FeedService.ClassifiedPost> page1 = feedService.generateFeed(currentProfile, 1, 10);

        // Assert
        assertThat(page0).hasSize(10);
        assertThat(page1).hasSize(10);
    }

    @Test
    void generateFeed_pagination_beyondAvailablePosts_shouldReturnEmptyList() {
        // Arrange
        List<Post> fewPosts = createRandomPosts(5);

        when(followRepository.findFollowedUserIds(currentProfileId)).thenReturn(Collections.emptyList());
        when(followRepository.findSecondDegreeConnectionIds(currentProfileId)).thenReturn(Collections.emptyList());
        when(followRepository.findFollowerIds(currentProfileId)).thenReturn(Collections.emptyList());
        when(profileRepository.findByOrganization(eq(ProfileType.organization), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
        when(postRepository.findPostByOrganizationAndNotDeleted(any(), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
        when(postRepository.findRecentPostsExcludingUsers(any(), any(PageRequest.class)))
                .thenReturn(fewPosts);

        // Act
        List<FeedService.ClassifiedPost> result = feedService.generateFeed(currentProfile, 10, 10);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void generateFeed_shouldRemoveDuplicatePosts() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Profile profile = createProfile(userId, "dupuser", ProfileType.user);
        Post duplicatePost = createPost(profile, 0);

        when(followRepository.findFollowedUserIds(currentProfileId)).thenReturn(List.of(userId));
        when(followRepository.findSecondDegreeConnectionIds(currentProfileId)).thenReturn(Collections.emptyList());
        when(followRepository.findFollowerIds(currentProfileId)).thenReturn(Collections.emptyList());
        // Same post returned from multiple sources
        when(postRepository.findByUserIdsAndNotDeleted(eq(List.of(userId)), any(PageRequest.class)))
                .thenReturn(List.of(duplicatePost));
        when(profileRepository.findByOrganization(eq(ProfileType.organization), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
        when(postRepository.findPostByOrganizationAndNotDeleted(any(), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
        when(postRepository.findRecentPostsExcludingUsers(any(), any(PageRequest.class)))
                .thenReturn(List.of(duplicatePost)); // Same post again

        // Act
        List<FeedService.ClassifiedPost> result = feedService.generateFeed(currentProfile, 0, 10);

        // Assert - should only have one instance of the duplicate post
        long count = result.stream()
                .filter(cp -> cp.post().getId().equals(duplicatePost.getId()))
                .count();
        assertThat(count).isEqualTo(1);
    }

    @Test
    void generateFeed_withNullProfileOnPost_shouldClassifyAsRandom() {
        // Arrange
        Post postWithNullProfile = new Post();
        postWithNullProfile.setId(UUID.randomUUID());
        postWithNullProfile.setProfile(null);
        postWithNullProfile.setCreatedAt(Instant.now());
        postWithNullProfile.setLikesCount(0);

        when(followRepository.findFollowedUserIds(currentProfileId)).thenReturn(Collections.emptyList());
        when(followRepository.findSecondDegreeConnectionIds(currentProfileId)).thenReturn(Collections.emptyList());
        when(followRepository.findFollowerIds(currentProfileId)).thenReturn(Collections.emptyList());
        when(profileRepository.findByOrganization(eq(ProfileType.organization), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
        when(postRepository.findPostByOrganizationAndNotDeleted(any(), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
        when(postRepository.findRecentPostsExcludingUsers(any(), any(PageRequest.class)))
                .thenReturn(List.of(postWithNullProfile));

        // Act
        List<FeedService.ClassifiedPost> result = feedService.generateFeed(currentProfile, 0, 10);

        // Assert
        assertThat(result).isNotEmpty();
        FeedService.ClassifiedPost classifiedPost = result.get(0);
        assertThat(classifiedPost.type()).isEqualTo(PostType.RANDOM);
    }

    @Test
    void generateFeed_organizationPriority_shouldClassifyAsOrganizationEvenIfFollowed() {
        // Arrange - Organization that user also follows
        UUID orgId = UUID.randomUUID();
        Profile orgProfile = createProfile(orgId, "followedorg", ProfileType.organization);
        Post orgPost = createPost(orgProfile, 0);

        when(followRepository.findFollowedUserIds(currentProfileId)).thenReturn(List.of(orgId));
        when(followRepository.findSecondDegreeConnectionIds(currentProfileId)).thenReturn(Collections.emptyList());
        when(followRepository.findFollowerIds(currentProfileId)).thenReturn(Collections.emptyList());
        when(postRepository.findByUserIdsAndNotDeleted(eq(List.of(orgId)), any(PageRequest.class)))
                .thenReturn(List.of(orgPost));
        when(profileRepository.findByOrganization(eq(ProfileType.organization), any(PageRequest.class)))
                .thenReturn(List.of(orgId));
        when(postRepository.findPostByOrganizationAndNotDeleted(eq(List.of(orgId)), any(PageRequest.class)))
                .thenReturn(List.of(orgPost));
        when(postRepository.findRecentPostsExcludingUsers(any(), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());

        // Act
        List<FeedService.ClassifiedPost> result = feedService.generateFeed(currentProfile, 0, 10);

        // Assert - Organization type should take priority
        assertThat(result).isNotEmpty();
        FeedService.ClassifiedPost classifiedPost = result.stream()
                .filter(cp -> cp.post().getId().equals(orgPost.getId()))
                .findFirst()
                .orElse(null);
        assertThat(classifiedPost).isNotNull();
        assertThat(classifiedPost.type()).isEqualTo(PostType.ORGANIZATION);
    }

    // ========== Helper Methods ==========

    private Profile createProfile(UUID id, String username, ProfileType type) {
        Profile profile = new Profile();
        profile.setId(id);
        profile.setUsername(username);
        profile.setType(type);
        return profile;
    }

    private Post createPost(Profile profile, int likes) {
        Post post = new Post();
        post.setId(UUID.randomUUID());
        post.setProfile(profile);
        post.setDescription("Test post description");
        post.setBlobName("test-blob.jpg");
        post.setLikesCount(likes);
        post.setCreatedAt(Instant.now());
        return post;
    }

    private List<Post> createRandomPosts(int count) {
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            UUID userId = UUID.randomUUID();
            Profile profile = createProfile(userId, "user" + i, ProfileType.user);
            posts.add(createPost(profile, 0));
        }
        return posts;
    }
}
