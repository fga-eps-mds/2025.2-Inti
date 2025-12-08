package br.mds.inti.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.mds.inti.model.dto.SearchProfile;
import br.mds.inti.service.SearchProfileService;

    @RestController
    @RequestMapping("/search")
    public class SearchProfileController {

        @Autowired
        private SearchProfileService searchProfileService;

        @GetMapping("/{username}")
        public ResponseEntity<SearchProfile> getPublicProfile(@PathVariable String username) {
            return ResponseEntity.ok().body(searchProfileService.getProfileByUsername(username));
        }
    }
