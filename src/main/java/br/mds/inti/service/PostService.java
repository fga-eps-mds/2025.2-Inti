package br.mds.inti.service;

import br.mds.inti.model.entity.Post;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.repositories.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

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
            blobName = blobService.uploadImageWithDescription(profile.getId(), image);
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

    public void deletePost(Profile profile, UUID postId) {

        Optional<Post> postOptional = postRepository.findById(postId);
        if (postOptional.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");

        Post post = postOptional.get();
        if (post.getProfile().getId() != profile.getId())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not the owner of the post");

        blobService.deleteImage(post.getBlobName());
        postRepository.softDeletePost(postId);
    }
}
