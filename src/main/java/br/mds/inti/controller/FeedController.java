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

import br.mds.inti.model.entity.Post;
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
            @RequestParam(name = "size", defaultValue = "20") int size) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Profile)) {
            return ResponseEntity.status(401).build();
        }
        Profile currentProfile = (Profile) auth.getPrincipal();

        List<Post> posts = feedService.generateFeed(currentProfile, size);

        List<FeedItem> items = posts.stream()
                .map(post -> {
                    PostType type = classifyPost(post, currentProfile);
                    String reason = getReasonForPost(post, type, currentProfile);
                    return new FeedItem(
                            post.getId(),
                            post.getDescription(),
                            post.getBlobName() == null ? null : "/images/" + post.getBlobName(),
                            post.getLikesCount(),
                            type,
                            reason);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(items);
    }

    @GetMapping("/organization")
    public ResponseEntity<String> getOrganizationDashboard() {
        return ResponseEntity.ok("Bem-vindo à área exclusiva de organizações!");
    }

    private PostType classifyPost(Post post, Profile currentProfile) {
        if (post.getProfile() != null && post.getProfile().getType() != null
                && post.getProfile().getType().name().equalsIgnoreCase("organization")) {
            return PostType.ORGANIZATION;
        }
        if (post.getProfile() != null && post.getProfile().getId().equals(currentProfile.getId())) {
            return PostType.FOLLOWED; // own or followed - simplified
        }
        if (post.getLikesCount() != null && post.getLikesCount() > 10) {
            return PostType.POPULAR;
        }
        return PostType.RANDOM;
    }

    private String getReasonForPost(Post post, PostType type, Profile currentProfile) {
        return switch (type) {
            case ORGANIZATION -> "Post de organização";
            case FOLLOWED -> "Perfil seguido / próprio";
            case POPULAR -> "Post popular";
            case SECOND_DEGREE -> "Conexão de segundo grau"; // currently not distinguished
            case RANDOM -> "Descoberta";
        };
    }
}
