package br.mds.inti.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.mds.inti.model.dto.FollowResponse;
import br.mds.inti.model.dto.ProfileResponse;
import br.mds.inti.service.FollowService;
import br.mds.inti.service.ProfileService;

@RestController
@RequestMapping("/profiles")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private FollowService followService;

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMe(@RequestParam("size") Integer size,
            @RequestParam("page") Integer page) {
        return ResponseEntity.ok().body(profileService.getProfile(page, size));
    }

    @GetMapping("/{username}")
    public ResponseEntity<ProfileResponse> getPublicProfile(@PathVariable String username,
            @RequestParam("size") Integer size, @RequestParam("page") Integer page) {
        return ResponseEntity.ok().body(profileService.getProfileByUsername(username, page, size));
    }

    @GetMapping("/string/teste/user")
    public ResponseEntity<String> getString() {

        return ResponseEntity.ok("teste");
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

    // jwt
    // 2:eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJsdWNhczIiLCJleHAiOjE3NjUxNjQ3MDN9.T0uW9DTG-HZ_un1wY-3iqaUM_8gpvz9G9_-63K6lz1I
}
