package br.mds.inti.controller;

import br.mds.inti.model.entity.Profile;
import br.mds.inti.repositories.ProfileRepository;
import br.mds.inti.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/post")
public class PostController {

    @Autowired
    PostService postService;

    @Autowired
    ProfileRepository profileRepository;

    @PostMapping()
    public ResponseEntity<String> createPost(@RequestPart MultipartFile image, @RequestPart String description) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Profile profile = (Profile) authentication.getPrincipal();

        postService.createPost(profile, image, description);
        return ResponseEntity.ok("Post created successfully!");
    }

    @DeleteMapping()
    public ResponseEntity<String> deletePost(UUID postId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Profile profile = (Profile) authentication.getPrincipal();

        postService.deletePost(profile, postId);
        return ResponseEntity.ok("Post deleted successfully");
    }
}
