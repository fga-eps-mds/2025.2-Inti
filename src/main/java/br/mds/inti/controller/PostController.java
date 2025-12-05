package br.mds.inti.controller;

import br.mds.inti.model.dto.PostDetailResponse;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.service.LikeService;
import br.mds.inti.service.PostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/post")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private LikeService likeService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Void> createPost(
            @Valid @NotNull @RequestPart MultipartFile image,
            @Valid @NotBlank @RequestPart String description) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Profile profile = (Profile) authentication.getPrincipal();

        postService.createPost(profile, image, description);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable @NotNull UUID postId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Profile profile = (Profile) authentication.getPrincipal();

        postService.deletePost(profile, postId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> getPostById(@PathVariable UUID postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Profile profile = null;
        if (authentication != null && authentication.getPrincipal() instanceof Profile authenticatedProfile) {
            profile = authenticatedProfile;
        }

        PostDetailResponse post = postService.getPostById(postId, profile);
        return ResponseEntity.ok(post);
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<Void> likePost(@PathVariable UUID postId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Profile profile = (Profile) authentication.getPrincipal();

        likeService.likePost(profile, postId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<Void> unlikePost(@PathVariable UUID postId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Profile profile = (Profile) authentication.getPrincipal();

        likeService.unlikePost(profile, postId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
