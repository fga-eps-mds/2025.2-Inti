package br.mds.inti.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import br.mds.inti.model.entity.Post;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.model.enums.PostType;
import br.mds.inti.model.enums.ProfileType;
import br.mds.inti.repositories.FollowRepository;
import br.mds.inti.repositories.PostRepository;
import br.mds.inti.repositories.ProfileRepository;

@Service
public class FeedService {

    private static final int MAX_POSTS_FROM_SAME_USER = 3;
    private static final double FOLLOWED_RATIO = 0.3; // 30% de posts de seguidos
    private static final double SECOND_DEGREE_RATIO = 0.2; // 20% de segunda conexao
    private static final double ORGANIZATION_RATIO = 0.3; // 30% de organização

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private ProfileRepository profileRepository;

    public record ClassifiedPost(Post post, PostType type, String reason) {
    }

    public List<ClassifiedPost> generateFeed(Profile currentProfile, int page, int pageSize) {

        // Buscar IDs
        List<UUID> followedIdsList = followRepository.findFollowedUserIds(currentProfile.getId());
        Set<UUID> followedIds = new HashSet<>(followedIdsList);

        List<UUID> secondDegreeIdsList = followRepository.findSecondDegreeConnectionIds(currentProfile.getId());
        Set<UUID> secondDegreeIds = new HashSet<>(secondDegreeIdsList);

        List<Post> allPosts = new ArrayList<>();

        // Coletar posts de TODAS as fontes (sempre página 0, com limites maiores)
        int poolSize = pageSize * 10; // Buscar pool maior para ter variedade

        // POSTS DE SEGUIDOS
        if (!followedIdsList.isEmpty()) {
            int followedLimit = Math.max(10, (int) (poolSize * FOLLOWED_RATIO));
            List<Post> followedPosts = getFollowedUsersPosts(followedIdsList, followedLimit);
            allPosts.addAll(followedPosts);
        }

        // POSTS SEGUNDO GRAU
        int secondDegreeLimit = Math.max(10, (int) (poolSize * SECOND_DEGREE_RATIO));
        List<Post> secondDegreePosts = getSecondDegreePosts(currentProfile.getId(), secondDegreeLimit);
        allPosts.addAll(secondDegreePosts);

        // POSTS ORGANIZAÇÃO
        int organizationLimit = Math.max(10, (int) (poolSize * ORGANIZATION_RATIO));
        List<Post> organizationPosts = getOrganizationPost(organizationLimit);
        allPosts.addAll(organizationPosts);

        // RANDOM (preenche o resto)
        int randomLimit = Math.max(20, poolSize - allPosts.size());
        List<Post> randomPosts = getRandomPosts(currentProfile.getId(), randomLimit);
        allPosts.addAll(randomPosts);

        // Remove duplicatas (mesmo post pode vir de várias fontes)
        Set<UUID> seenIds = new HashSet<>();
        List<Post> uniquePosts = allPosts.stream()
                .filter(post -> seenIds.add(post.getId()))
                .collect(Collectors.toList());

        // ORDERNAÇÃO FINAL
        List<Post> sortedPosts = applyFinalSorting(uniquePosts);

        // PAGINAÇÃO REAL (aplicada no resultado agregado)
        int start = page * pageSize;
        int end = Math.min(start + pageSize, sortedPosts.size());

        List<Post> pagedPosts = (start < sortedPosts.size())
                ? sortedPosts.subList(start, end)
                : Collections.emptyList();

        // Classificar e retornar
        return pagedPosts.stream()
                .map(post -> {
                    PostType type = classifyPost(post, currentProfile, followedIds, secondDegreeIds);
                    String reason = getReasonForPost(type);
                    return new ClassifiedPost(post, type, reason);
                })
                .collect(Collectors.toList());
    }

    private PostType classifyPost(Post post, Profile currentProfile, Set<UUID> followedIds, Set<UUID> secondDegreeIds) {
        if (post.getProfile() == null) {
            return PostType.RANDOM;
        }

        UUID postAuthorId = post.getProfile().getId();

        // 1. Verifica se é post de organização
        if (post.getProfile().getType() != null
                && post.getProfile().getType().name().equalsIgnoreCase("organization")) {
            return PostType.ORGANIZATION;
        }

        // 2. Verifica se é próprio post
        if (postAuthorId.equals(currentProfile.getId())) {
            return PostType.FOLLOWED;
        }

        // 3. Verifica se é de alguém que você segue diretamente
        if (followedIds.contains(postAuthorId)) {
            return PostType.FOLLOWED;
        }

        // 4. Verifica se é de conexão de segundo grau
        if (secondDegreeIds.contains(postAuthorId)) {
            return PostType.SECOND_DEGREE;
        }

        // 5. Verifica se é popular
        if (post.getLikesCount() != null && post.getLikesCount() > 10) {
            return PostType.POPULAR;
        }

        // 6. Caso contrário, é aleatório
        return PostType.RANDOM;
    }

    private String getReasonForPost(PostType type) {
        return switch (type) {
            case ORGANIZATION -> "Post de organização";
            case FOLLOWED -> "Perfil seguido / próprio";
            case POPULAR -> "Post popular";
            case SECOND_DEGREE -> "Conexão de segundo grau";
            case RANDOM -> "Descoberta";
        };
    }

    private List<Post> getFollowedUsersPosts(List<UUID> followedIds, int limit) {
        // sempre página 0
        List<Post> posts = postRepository.findByUserIdsAndNotDeleted(
                followedIds,
                PageRequest.of(0, limit * 2));

        return limitPostsPerUser(posts, MAX_POSTS_FROM_SAME_USER, limit);
    }

    private List<Post> getOrganizationPost(int limit) {

        List<UUID> organizationsIds = profileRepository.findByOrganization(
                ProfileType.organization,
                PageRequest.of(0, 10));

        return postRepository.findPostByOrganizationAndNotDeleted(
                organizationsIds,
                PageRequest.of(0, limit));
    }

    private List<Post> getSecondDegreePosts(UUID currentUserId, int limit) {

        List<UUID> myFollowers = followRepository.findFollowerIds(currentUserId);
        if (myFollowers.isEmpty())
            return Collections.emptyList();

        List<UUID> followedByMyFollowers = followRepository.findFollowedByUsers(myFollowers);
        List<UUID> iFollow = followRepository.findFollowedUserIds(currentUserId);

        List<UUID> secondDegreeUsers = followedByMyFollowers.stream()
                .filter(id -> !id.equals(currentUserId))
                .filter(id -> !iFollow.contains(id))
                .distinct()
                .toList();

        if (secondDegreeUsers.isEmpty())
            return Collections.emptyList();

        return postRepository.findByUserIdsAndNotDeleted(
                secondDegreeUsers,
                PageRequest.of(0, limit));
    }

    private List<Post> getRandomPosts(UUID currentUserId, int limit) {

        List<UUID> ids = new ArrayList<>(followRepository.findFollowedUserIds(currentUserId));
        ids.add(currentUserId);

        return postRepository.findRecentPostsExcludingUsers(
                ids,
                PageRequest.of(0, limit));
    }

    private List<Post> applyFinalSorting(List<Post> posts) {
        return posts.stream().sorted(Comparator.comparing(this::calculatePostScore).reversed())
                .collect(Collectors.toList());
    }

    private double calculatePostScore(Post post) {
        double score = 0;

        long hoursOld = Duration.between(post.getCreatedAt(), Instant.now()).toHours();

        if (hoursOld <= 24) {
            score += (24 - hoursOld) * 0.5;
        }

        if (post.getLikesCount() != null) {
            score += Math.log(post.getLikesCount() + 1) * 0.3;
        }

        // ruído controlado para dar variedade
        score += Math.random() * 0.5;

        return score;
    }

    private List<Post> limitPostsPerUser(List<Post> posts, int maxPerUser, int totalLimit) {

        Map<UUID, Integer> userPostCount = new HashMap<>();
        List<Post> result = new ArrayList<>();

        for (Post post : posts) {
            UUID userId = post.getProfile().getId();
            int count = userPostCount.getOrDefault(userId, 0);

            if (count < maxPerUser && result.size() < totalLimit) {
                result.add(post);
                userPostCount.put(userId, count + 1);
            }

            if (result.size() >= totalLimit) {
                break;
            }

        }

        return result;
    }
}
