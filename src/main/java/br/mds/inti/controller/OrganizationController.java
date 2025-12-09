package br.mds.inti.controller;

import br.mds.inti.model.dto.FollowResponse;
import br.mds.inti.model.dto.ProfileResponse;
import br.mds.inti.model.dto.UpdateUserRequest;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.service.FollowService;
import br.mds.inti.service.OrganizationService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@RestController
@RequestMapping("/org")
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private FollowService followService;

    @GetMapping
    public ResponseEntity<ProfileResponse> getMe(@RequestParam("size") Integer size,
            @RequestParam("page") Integer page) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null ||  !(auth.getPrincipal() instanceof Profile profile)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");

        return ResponseEntity.ok().body(organizationService.getOrganization(page, size, profile));
    }

    @GetMapping("/{username}")
    public ResponseEntity<ProfileResponse> getPublicProfile(@PathVariable String username,
            @RequestParam("size") Integer size, @RequestParam("page") Integer page) {
        return ResponseEntity.ok().body(organizationService.getOrganizationByUsername(username, page, size));
    }

    @PostMapping
    public ResponseEntity<Void> setMyOrgnizationPhoto(MultipartFile myImage) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if(auth == null ||  !(auth.getPrincipal() instanceof Profile profile)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");

            organizationService.setPhoto(myImage, profile);
            return ResponseEntity.status(HttpStatus.CREATED).build();

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error trying to set organization image");

        }
    }

    @PatchMapping
    public ResponseEntity<Void> updateOrganization(@NotNull @ModelAttribute UpdateUserRequest updateUserRequest) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if(auth == null ||  !(auth.getPrincipal() instanceof Profile profile)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");

            organizationService.updateOrganization(updateUserRequest, profile);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error trying to update organization");
        }
    }

    @PostMapping("/{username}/follow")
    public ResponseEntity<FollowResponse> followProfile(@PathVariable String username) {
        FollowResponse response = followService.followProfile(username);

        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{username}/unfollow")
    public ResponseEntity<FollowResponse> unfollowOrganization(@PathVariable String username) {
        FollowResponse response = followService.unfollowProfile(username);

        return ResponseEntity.ok().body(response);
    }
}
