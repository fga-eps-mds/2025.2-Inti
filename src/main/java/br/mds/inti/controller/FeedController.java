package br.mds.inti.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.mds.inti.model.entity.Profile;
import br.mds.inti.model.enums.PostType;
import br.mds.inti.service.FeedService;

//    @PreAuthorize("hasRole('ORGANIZATION')")

@RestController
@RequestMapping("/feed")
public class FeedController {

    @Autowired
    private FeedService feedService;

    public record FeedItem(
            UUID id,
            String description,
            String imageUrl,
            Integer likes,
            PostType type,
            String reason) {
    }

    @GetMapping
    public ResponseEntity<List<FeedItem>> getFeedWithMetadata(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Profile)) {
            return ResponseEntity.status(401).build();
        }
        Profile currentProfile = (Profile) auth.getPrincipal();

        List<FeedService.ClassifiedPost> classifiedPosts = feedService.generateFeed(currentProfile, page, size);

        List<FeedItem> items = classifiedPosts.stream()
                .map(cp -> new FeedItem(
                        cp.post().getId(),
                        cp.post().getDescription(),
                        cp.post().getBlobName() == null ? null : "/images/" + cp.post().getBlobName(),
                        cp.post().getLikesCount(),
                        cp.type(),
                        cp.reason()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(items);
    }

    @GetMapping("/organization")
    public ResponseEntity<String> getOrganizationDashboard() {
        return ResponseEntity.ok("Bem-vindo à área exclusiva de organizações!");
    }
}
