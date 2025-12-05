package br.mds.inti.service;

import br.mds.inti.model.dto.PostDetailResponse;
import br.mds.inti.model.entity.Like;
import br.mds.inti.model.entity.Post;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.repositories.LikeRepository;
import br.mds.inti.repositories.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Optional;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private BlobService blobService;

    @Mock
    private LikeRepository likeRepository;

    @InjectMocks
    private PostService postService;

    @Test
    void createPost_success_shouldSavePostWithBlobNameAndDescription() throws Exception {
        Profile profile = new Profile();
        UUID profileId = UUID.randomUUID();
        profile.setId(profileId);

        String description = "a lovely picture";
        MockMultipartFile image = new MockMultipartFile("image", "pic.png", "image/png", "hello".getBytes());

        String expectedBlob = "user-" + profileId + "_123.png";
        when(blobService.uploadImage(eq(profileId), any(MultipartFile.class))).thenReturn(expectedBlob);

        postService.createPost(profile, image, description);

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());

        Post saved = captor.getValue();
        assertThat(saved.getDescription()).isEqualTo(description);
        assertThat(saved.getBlobName()).isEqualTo(expectedBlob);
        assertThat(saved.getLikesCount()).isEqualTo(0);
        assertThat(saved.getProfile()).isSameAs(profile);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void createPost_whenBlobUploadThrowsIOException_shouldReturn500() throws Exception {
        Profile profile = new Profile();
        UUID profileId = UUID.randomUUID();
        profile.setId(profileId);

        MockMultipartFile image = new MockMultipartFile("image", "pic.png", "image/png", "hello".getBytes());

        when(blobService.uploadImage(eq(profileId), any(MultipartFile.class)))
                .thenThrow(new IOException("boom"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> postService.createPost(profile, image, "desc"));

        assertThat(ex.getStatusCode().value()).isEqualTo(500);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void deletePost_whenPostNotFound_shouldThrow404() {
        Profile profile = new Profile();
        profile.setId(UUID.randomUUID());

        UUID postId = UUID.randomUUID();

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> postService.deletePost(profile, postId));

        assertThat(ex.getStatusCode().value()).isEqualTo(404);
        verify(blobService, never()).deleteImage(any());
    }

    @Test
    void deletePost_whenUserNotOwner_shouldThrow401_andNotDelete() {
        Profile owner = new Profile();
        owner.setId(UUID.randomUUID());

        Profile caller = new Profile();
        caller.setId(UUID.randomUUID()); // different id

        Post post = new Post();
        post.setId(UUID.randomUUID());
        post.setProfile(owner);
        post.setBlobName("blob-name.png");

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> postService.deletePost(caller, post.getId()));

        assertThat(ex.getStatusCode().value()).isEqualTo(401);
        verify(blobService, never()).deleteImage(any());
    }

    @Test
    void deletePost_success_shouldDeleteBlobAndSoftDeletePost() {
        Profile profile = new Profile();
        profile.setId(UUID.randomUUID());

        Post post = new Post();
        UUID postId = UUID.randomUUID();
        post.setId(postId);
        // IMPORTANT: PostService compares object references (==) for ids, so keep same
        // UUID instance
        // but to be safe, set the same profile instance on both
        post.setProfile(profile);
        post.setBlobName("blob-1.png");

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        postService.deletePost(profile, postId);

        verify(blobService).deleteImage("blob-1.png");
        verify(postRepository).softDeletePost(postId);
    }

    @Test
    void getPostById_whenPostExists_shouldReturnPostDetail() {
        UUID postId = UUID.randomUUID();
        Profile author = new Profile();
        author.setId(UUID.randomUUID());
        author.setName("Author Name");
        author.setUsername("author_user");
        author.setProfilePictureUrl("http://pic.url");

        Post post = new Post();
        post.setId(postId);
        post.setProfile(author);
        post.setDescription("Desc");
        post.setLikesCount(0);
        post.setCreatedAt(Instant.now());
        post.setBlobName("blob.png");

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        Profile viewer = buildProfile();
        when(likeRepository.findByProfileAndPostId(viewer, postId)).thenReturn(Optional.empty());

        PostDetailResponse response = postService.getPostById(postId, viewer);

        assertThat(response.id()).isEqualTo(postId);
        assertThat(response.description()).isEqualTo("Desc");
        assertThat(response.author().username()).isEqualTo("author_user");
        assertThat(response.imageUrl()).isEqualTo("/images/blob.png");
    }

    @Test
    void getPostById_whenPostNotFound_shouldThrow404() {
        UUID postId = UUID.randomUUID();
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> postService.getPostById(postId, buildProfile()));

        assertThat(ex.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void getPostById_whenPostDeleted_shouldThrow404() {
        UUID postId = UUID.randomUUID();
        Post post = new Post();
        post.setId(postId);
        post.setDeletedAt(Instant.now());

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> postService.getPostById(postId, buildProfile()));

        assertThat(ex.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void getPostById_whenLikedByCurrentProfile_shouldReturnLikedTrue() {
        UUID postId = UUID.randomUUID();
        Profile author = new Profile();
        author.setId(UUID.randomUUID());

        Post post = new Post();
        post.setId(postId);
        post.setProfile(author);
        post.setDescription("Desc");
        post.setLikesCount(5);
        post.setCreatedAt(Instant.now());
        post.setBlobName("blob.png");

        Profile viewer = buildProfile();

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(likeRepository.findByProfileAndPostId(viewer, postId)).thenReturn(Optional.of(new Like()));

        PostDetailResponse response = postService.getPostById(postId, viewer);

        assertThat(response.liked()).isTrue();
    }

    @Test
    void getPostById_whenProfileIsNull_shouldSkipLikeLookup() {
        UUID postId = UUID.randomUUID();
        Profile author = new Profile();
        author.setId(UUID.randomUUID());

        Post post = new Post();
        post.setId(postId);
        post.setProfile(author);
        post.setDescription("Desc");
        post.setLikesCount(5);
        post.setCreatedAt(Instant.now());

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        PostDetailResponse response = postService.getPostById(postId, null);

        assertThat(response.liked()).isFalse();
        verify(likeRepository, never()).findByProfileAndPostId(any(Profile.class), any(UUID.class));
    }

    private Profile buildProfile() {
        Profile profile = new Profile();
        profile.setId(UUID.randomUUID());
        profile.setUsername("viewer");
        return profile;
    }
}
