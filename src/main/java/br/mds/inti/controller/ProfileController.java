package br.mds.inti.controller;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import br.mds.inti.model.dto.ProductSummaryDTO;
import br.mds.inti.model.dto.ProfileSearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import br.mds.inti.model.dto.FollowResponse;
import br.mds.inti.model.dto.ProfileResponse;
import br.mds.inti.model.dto.UpdateUserRequest;
import br.mds.inti.service.FollowService;
import br.mds.inti.service.ProductService;
import br.mds.inti.service.ProfileService;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private FollowService followService;

    @Autowired
    private ProductService productService;

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

    @GetMapping("/{profileId}/products")
    public ResponseEntity<Page<ProductSummaryDTO>> getProfileProducts(
            @PathVariable UUID profileId,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<ProductSummaryDTO> products = productService.getProfileProducts(profileId, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProfileSearchResponse>> searchProfiles(
            @RequestParam("query") String query,
            @RequestParam(name = "limit", defaultValue = "5") Integer limit) {

        return ResponseEntity.ok(profileService.searchProfiles(query, limit));
    }

}
