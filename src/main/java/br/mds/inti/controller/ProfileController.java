package br.mds.inti.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profiles")
public class ProfileController {

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
