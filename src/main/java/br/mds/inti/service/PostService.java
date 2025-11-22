package br.mds.inti.service;

import br.mds.inti.model.dto.PostDetailResponse;
import br.mds.inti.model.dto.PostResponse;
import br.mds.inti.model.dto.UserSummaryResponse;
import br.mds.inti.model.entity.Post;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.repositories.PostRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PostService {

    @Autowired
    PostRepository postRepository;

    @Autowired
    BlobService blobService;

    public void createPost(Profile profile, MultipartFile image, String description) {
        String blobName = "";
        try {
            blobName = blobService.uploadImage(profile.getId(), image);
        } catch (IOException e) {
            log.error("error", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image");
        }

        Post post = new Post();
        post.setCreatedAt(Instant.now());
        post.setDescription(description);
        post.setBlobName(blobName);
        post.setLikesCount(0);
        post.setProfile(profile);

        postRepository.save(post);
    }

    @Transactional
    public void deletePost(Profile profile, UUID postId) {

        Optional<Post> postOptional = postRepository.findById(postId);
        if (postOptional.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");

        Post post = postOptional.get();
        if (!post.getProfile().getId().equals(profile.getId())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not the owner of the post");

        blobService.deleteImage(post.getBlobName());
        postRepository.softDeletePost(postId);
    }

    public Page<PostResponse> getPostByIdProfile(UUID profileId, Pageable peageble) {

        Page<Post> postByprofile = postRepository.findAllByProfileIdAndNotDeleted(profileId, peageble);

        return postByprofile.map(post -> new PostResponse(post.getId(),
                generateImageUrl(post.getBlobName()),
                post.getDescription(),
                post.getLikesCount(),
                post.getCreatedAt().toString()));

    }

    private String generateImageUrl(String blobName) {
        if (blobName == null || blobName.isEmpty()) {
            return null;
        }
        return "/images/" + blobName;
    }

    public PostDetailResponse getPostById(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if (post.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }

        UserSummaryResponse author = new UserSummaryResponse(
                post.getProfile().getId(),
                post.getProfile().getName(),
                post.getProfile().getUsername(),
                post.getProfile().getProfilePictureUrl());

        List<UserSummaryResponse> likedBy = post.getLikes().stream()
                .map(like -> new UserSummaryResponse(
                        like.getProfile().getId(),
                        like.getProfile().getName(),
                        like.getProfile().getUsername(),
                        like.getProfile().getProfilePictureUrl()))
                .collect(Collectors.toList());

        return new PostDetailResponse(
                post.getId(),
                generateImageUrl(post.getBlobName()),
                post.getDescription(),
                post.getLikesCount(),
                post.getCreatedAt().toString(),
                author,
                likedBy);
    }
}
