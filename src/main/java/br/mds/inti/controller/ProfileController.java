package br.mds.inti.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.mds.inti.model.dto.ProfileResponse;
import br.mds.inti.service.ProfileService;

@RestController
@RequestMapping("/profiles")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

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

    @GetMapping("/string/teste/organization")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public ResponseEntity<String> getStringOrg() {

        return ResponseEntity.ok("teste");
    }

}
