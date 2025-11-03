package br.mds.inti.service;

import br.mds.inti.model.entity.Post;
import br.mds.inti.model.entity.Profile;
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
        when(blobService.uploadImageWithDescription(eq(profileId), any(MultipartFile.class))).thenReturn(expectedBlob);

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

        when(blobService.uploadImageWithDescription(eq(profileId), any(MultipartFile.class))).thenThrow(new IOException("boom"));

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
        // IMPORTANT: PostService compares object references (==) for ids, so keep same UUID instance
        // but to be safe, set the same profile instance on both
        post.setProfile(profile);
        post.setBlobName("blob-1.png");

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        postService.deletePost(profile, postId);

        verify(blobService).deleteImage("blob-1.png");
        verify(postRepository).softDeletePost(postId);
    }
}

