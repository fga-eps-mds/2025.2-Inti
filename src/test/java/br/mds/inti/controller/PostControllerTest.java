package br.mds.inti.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import br.mds.inti.model.dto.PostDetailResponse;
import br.mds.inti.model.dto.UserSummaryResponse;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.service.LikeService;
import br.mds.inti.service.PostService;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    @Mock
    private PostService postService;

    @Mock
    private LikeService likeService;

    @InjectMocks
    private PostController postController;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getPostById_withAuthenticatedProfile_shouldDelegateToService() {
        UUID postId = UUID.randomUUID();
        Profile profile = buildProfile();
        mockSecurityContext(profile);

        PostDetailResponse expected = buildPostDetailResponse(postId);
        when(postService.getPostById(postId, profile)).thenReturn(expected);

        ResponseEntity<PostDetailResponse> response = postController.getPostById(postId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(postService).getPostById(postId, profile);
    }

    @Test
    void getPostById_withoutAuthenticatedProfile_shouldPassNullToService() {
        UUID postId = UUID.randomUUID();
        mockSecurityContext("anonymousUser");

        PostDetailResponse expected = buildPostDetailResponse(postId);
        when(postService.getPostById(postId, null)).thenReturn(expected);

        ResponseEntity<PostDetailResponse> response = postController.getPostById(postId);

        assertThat(response.getBody()).isEqualTo(expected);
        verify(postService).getPostById(postId, null);
    }

    @Test
    void likePost_shouldCallLikeServiceAndReturnOk() {
        UUID postId = UUID.randomUUID();
        Profile profile = buildProfile();
        mockSecurityContext(profile);

        ResponseEntity<Void> response = postController.likePost(postId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(likeService).likePost(profile, postId);
    }

    @Test
    void unlikePost_shouldCallLikeServiceAndReturnOk() {
        UUID postId = UUID.randomUUID();
        Profile profile = buildProfile();
        mockSecurityContext(profile);

        ResponseEntity<Void> response = postController.unlikePost(postId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(likeService).unlikePost(profile, postId);
    }

    private void mockSecurityContext(Object principal) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);
    }

    private Profile buildProfile() {
        Profile profile = new Profile();
        profile.setId(UUID.randomUUID());
        profile.setUsername("user");
        return profile;
    }

    private PostDetailResponse buildPostDetailResponse(UUID postId) {
        UserSummaryResponse author = new UserSummaryResponse(UUID.randomUUID(), "Author", "author", null);
        return new PostDetailResponse(
                postId,
                "/images/post.png",
                "Descricao",
                3,
                "2025-12-11T00:00:00Z",
                author,
                Collections.emptyList(),
                false);
    }
}
