package br.mds.inti.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import br.mds.inti.model.dto.FollowResponse;
import br.mds.inti.model.dto.ProfileResponse;
import br.mds.inti.model.dto.UpdateUserRequest;
import br.mds.inti.service.FollowService;
import br.mds.inti.service.ProfileService;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/profile")
public class ProfileController {
    @Autowired
    private ProfileService profileService;

    @Autowired
    private FollowService followService;

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMe(@RequestParam("size") Integer size,
            @RequestParam("page") Integer page) {
        ProfileResponse response = profileService.getProfile(page, size);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{username}")
    public ResponseEntity<ProfileResponse> getPublicProfile(@PathVariable String username,
            @RequestParam("size") Integer size, @RequestParam("page") Integer page) {
        return ResponseEntity.ok().body(profileService.getProfileByUsername(username, page, size));
    }

    @PostMapping("/upload-me")
    public ResponseEntity<Void> setMyProfilePhoto(MultipartFile myImage) {
        try {
            profileService.setPhoto(myImage);
            return ResponseEntity.status(HttpStatus.CREATED).build();

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error trying to set profile image");

        }
    }

    @PatchMapping("/update")
    public ResponseEntity<Void> user(@NotNull @ModelAttribute UpdateUserRequest updateUserRequest) {
        try {
            profileService.updateUser(updateUserRequest);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error trying to update profile");
        }
    }

    @PostMapping("/{username}/follow")
    public ResponseEntity<FollowResponse> followProfile(@PathVariable String username) {
        FollowResponse response = followService.followProfile(username);

        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{username}/unfollow")
    public ResponseEntity<FollowResponse> unfollowProfile(@PathVariable String username) {
        FollowResponse response = followService.unfollowProfile(username);

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/string/teste/organization")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<String> getStringOrg() {

        return ResponseEntity.ok("teste");
    }
    // jwt 1:
    // eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJsdWNhcyIsImV4cCI6MTc2NTE2NDcwM30.8qzxRDzIku8G-OXxHLl0Z7BFnJrS1MrMeITUaX139-E"

}
