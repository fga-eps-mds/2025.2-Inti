package br.mds.inti.controller;

import br.mds.inti.model.dto.auth.LoginRequest;
import br.mds.inti.model.dto.auth.ProfileCreationResponse;
import br.mds.inti.model.dto.auth.RegisterRequest;
import br.mds.inti.model.dto.auth.LoginResponse;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ProfileCreationResponse> register(@RequestBody RegisterRequest request) {
        ProfileCreationResponse response = authService.register(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping()
    public String getPosts(Profile user) {
        return "userid: ";
    }

}