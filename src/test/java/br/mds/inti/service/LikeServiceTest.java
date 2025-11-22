package br.mds.inti.service;

import br.mds.inti.model.entity.Like;
import br.mds.inti.model.entity.Post;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.repositories.LikeRepository;
import br.mds.inti.repositories.PostRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    LikeRepository likeRepository;

    @Mock
    PostRepository postRepository;

    @InjectMocks
    LikeService likeService;

    Profile profile;
    Post post;
    UUID postId;

    @BeforeEach
    void setUp() {
        profile = new Profile();
        profile.setId(UUID.randomUUID());
        postId = UUID.randomUUID();
        post = new Post();
        post.setId(postId);
        post.setLikesCount(0);
    }

    @Test
    void likePost_ShouldPersistLikeAndIncrementCount_WhenNotAlreadyLiked() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(likeRepository.findByProfileAndPostId(profile, postId)).thenReturn(Optional.empty());
        when(likeRepository.save(any(Like.class))).thenAnswer(inv -> inv.getArgument(0));

        doNothing().when(postRepository).updateLikesCount(eq(1), eq(postId));

        likeService.likePost(profile, postId);

        verify(likeRepository).save(any(Like.class));
        verify(postRepository).updateLikesCount(1, postId);
    }

    @Test
    void likePost_ShouldThrowConflict_WhenAlreadyLiked() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        Like existingLike = new Like();
        when(likeRepository.findByProfileAndPostId(profile, postId)).thenReturn(Optional.of(existingLike));

        assertThatThrownBy(() -> likeService.likePost(profile, postId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409 CONFLICT");

        verify(likeRepository, never()).save(any());
        verify(postRepository, never()).updateLikesCount(anyInt(), any());
    }

    @Test
    void likePost_ShouldThrowNotFound_WhenPostDoesNotExist() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.likePost(profile, postId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("404 NOT_FOUND");
    }

    @Test
    void unlikePost_ShouldDeleteLikeAndDecrementCount_WhenPostExists() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        Assertions.assertThrows(ResponseStatusException.class, () -> {
            likeService.unlikePost(profile, postId);
        });

    }

    @Test
    void unlikePost_ShouldThrowNotFound_WhenPostDoesNotExist() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.unlikePost(profile, postId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("404 NOT_FOUND");
    }
}

