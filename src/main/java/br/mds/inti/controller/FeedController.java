package br.mds.inti.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FeedController {

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello s");
    }

    @GetMapping("/organization")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public String getOrganizationDashboard() {
        return "Bem-vindo à área exclusiva de organizações!";
    }
}
