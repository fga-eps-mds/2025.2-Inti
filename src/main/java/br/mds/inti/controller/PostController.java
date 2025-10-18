package br.mds.inti.controller;

import br.mds.inti.model.Profile;
import br.mds.inti.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
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
    PostService postService;

    @PostMapping("/")
    public ResponseEntity<String> createPost(@RequestPart MultipartFile image, @RequestPart String type,
                                             @RequestPart String title, @RequestPart String description) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Profile profile = (Profile) authentication.getPrincipal();

        postService.createPost(profile, image, type, title, description);
        return ResponseEntity.ok("Post created successfully!");
    }

    @DeleteMapping("/")
    public ResponseEntity<String> deletePost(UUID postId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Profile profile = (Profile) authentication.getPrincipal();

        postService.deletePost(profile, postId);
    }
}
