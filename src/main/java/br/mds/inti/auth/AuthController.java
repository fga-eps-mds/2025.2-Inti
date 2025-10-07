package br.mds.inti.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.mds.inti.auth.dto.LoginRequest;
import br.mds.inti.auth.dto.RegisterRequest;
import br.mds.inti.models.Profile;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping()
    public String getPosts(Profile user) {
        return "userid: ";
    }

}