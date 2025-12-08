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
import java.util.Objects;
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
import br.mds.inti.repositories.LikeRepository;
import br.mds.inti.repositories.PostRepository;
import br.mds.inti.repositories.ProfileRepository;

@Service
public class FeedService {

    private static final int MAX_POSTS_FROM_SAME_USER = 2;
    private static final Duration FOLLOWED_POST_WINDOW = Duration.ofHours(24);
    private static final double FOLLOWED_RATIO = 0.3; // 30% de posts de seguidos
    private static final double SECOND_DEGREE_RATIO = 0.2; // 20% de segunda conexao
    private static final double ORGANIZATION_RATIO = 0.2; // 20% de organização

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private LikeRepository likeRepository;

    public record ClassifiedPost(Post post, PostType type, String reason, boolean liked) {
    }

    public List<ClassifiedPost> generateFeed(Profile currentProfile, int page, int pageSize) {

        UUID currentProfileId = currentProfile.getId();

        List<UUID> followedIdsList = new ArrayList<>(followRepository.findFollowedUserIds(currentProfileId));
        followedIdsList.removeIf(id -> id == null || id.equals(currentProfileId));
        Set<UUID> followedIds = new HashSet<>(followedIdsList);

        List<UUID> secondDegreeIdsList = new ArrayList<>(
                followRepository.findSecondDegreeConnectionIds(currentProfileId));
        secondDegreeIdsList.removeIf(Objects::isNull);
        Set<UUID> secondDegreeIds = new HashSet<>(secondDegreeIdsList);

        int followerQuota = calculateQuota(pageSize, FOLLOWED_RATIO);
        int secondDegreeQuota = calculateQuota(pageSize, SECOND_DEGREE_RATIO);
        int organizationQuota = calculateQuota(pageSize, ORGANIZATION_RATIO);
        int baseRandomQuota = Math.max(pageSize - (followerQuota + secondDegreeQuota + organizationQuota), 0);

        List<Post> followerPool = followerQuota > 0
                ? getFollowedUsersPosts(followedIdsList, currentProfileId,
                        calculateCandidatePoolSize(pageSize, followerQuota, page))
                : Collections.emptyList();
        List<Post> secondDegreePool = secondDegreeQuota > 0
                ? getSecondDegreePosts(currentProfileId,
                        calculateCandidatePoolSize(pageSize, secondDegreeQuota, page))
                : Collections.emptyList();
        List<Post> organizationPool = organizationQuota > 0
                ? getOrganizationPost(currentProfileId,
                        calculateCandidatePoolSize(pageSize, organizationQuota, page))
                : Collections.emptyList();

        List<Post> followerSlice = sliceForPage(followerPool, page, followerQuota);
        List<Post> secondDegreeSlice = sliceForPage(secondDegreePool, page, secondDegreeQuota);
        List<Post> organizationSlice = sliceForPage(organizationPool, page, organizationQuota);

        List<Post> stagedPosts = new ArrayList<>(pageSize);
        Set<UUID> usedPostIds = new HashSet<>();

        int followerAdded = addCategoryPosts(stagedPosts, followerSlice, followerQuota, usedPostIds, currentProfileId);
        int secondDegreeAdded = addCategoryPosts(stagedPosts, secondDegreeSlice, secondDegreeQuota, usedPostIds,
                currentProfileId);
        int organizationAdded = addCategoryPosts(stagedPosts, organizationSlice, organizationQuota, usedPostIds,
                currentProfileId);

        int followerDeficit = Math.max(followerQuota - followerAdded, 0);
        int secondDegreeDeficit = Math.max(secondDegreeQuota - secondDegreeAdded, 0);
        int organizationDeficit = Math.max(organizationQuota - organizationAdded, 0);

        int totalRandomNeeded = baseRandomQuota + followerDeficit + secondDegreeDeficit + organizationDeficit;

        if (totalRandomNeeded > 0) {
            List<Post> randomPool = getRandomPosts(currentProfileId,
                    calculateCandidatePoolSize(pageSize, totalRandomNeeded, page));
            addCategoryPosts(stagedPosts, randomPool, totalRandomNeeded, usedPostIds, currentProfileId);
        }

        if (stagedPosts.size() < pageSize) {
            int missing = pageSize - stagedPosts.size();
            List<Post> fallbackRandom = getRandomPosts(currentProfileId,
                    calculateCandidatePoolSize(pageSize, missing, page + 1));
            addCategoryPosts(stagedPosts, fallbackRandom, missing, usedPostIds, currentProfileId);
        }

        if (stagedPosts.isEmpty()) {
            return Collections.emptyList();
        }

        if (stagedPosts.size() > pageSize) {
            stagedPosts = new ArrayList<>(stagedPosts.subList(0, pageSize));
        }

        List<Post> sortedPosts = applyFinalSorting(stagedPosts);
        Set<UUID> likedPostIds = findLikedPostIds(currentProfileId, sortedPosts);

        return sortedPosts.stream()
                .map(post -> {
                    PostType type = classifyPost(post, currentProfile, followedIds, secondDegreeIds);
                    String reason = getReasonForPost(type);
                    boolean liked = likedPostIds.contains(post.getId());
                    return new ClassifiedPost(post, type, reason, liked);
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

    private List<Post> getFollowedUsersPosts(List<UUID> followedIds, UUID currentProfileId, int limit) {

        if (followedIds.isEmpty() || limit <= 0) {
            return Collections.emptyList();
        }

        List<Post> posts = postRepository.findByUserIdsAndNotDeleted(
                followedIds,
                PageRequest.of(0, limit));

        Instant cutoff = Instant.now().minus(FOLLOWED_POST_WINDOW);

        List<Post> recentPosts = posts.stream()
                .filter(post -> post.getProfile() != null)
                .filter(post -> !post.getProfile().getId().equals(currentProfileId))
                .filter(post -> post.getCreatedAt() != null && !post.getCreatedAt().isBefore(cutoff))
                .collect(Collectors.toList());

        return limitPostsPerUser(recentPosts, MAX_POSTS_FROM_SAME_USER, limit);
    }

    private List<Post> getOrganizationPost(UUID currentProfileId, int limit) {

        if (limit <= 0) {
            return Collections.emptyList();
        }

        List<UUID> organizationsIds = new ArrayList<>(profileRepository.findByOrganization(
                ProfileType.organization,
                PageRequest.of(0, Math.max(limit, 10))));

        organizationsIds.removeIf(id -> id == null || id.equals(currentProfileId));

        if (organizationsIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Post> posts = postRepository.findPostByOrganizationAndNotDeleted(
                organizationsIds,
                PageRequest.of(0, limit));

        return limitPostsPerUser(posts, MAX_POSTS_FROM_SAME_USER, limit);
    }

    private List<Post> getSecondDegreePosts(UUID currentUserId, int limit) {

        if (limit <= 0) {
            return Collections.emptyList();
        }

        List<UUID> myFollowers = followRepository.findFollowerIds(currentUserId);
        if (myFollowers.isEmpty())
            return Collections.emptyList();

        List<UUID> followedByMyFollowers = followRepository.findFollowedByUsers(myFollowers);
        List<UUID> iFollow = followRepository.findFollowedUserIds(currentUserId);

        List<UUID> secondDegreeUsers = followedByMyFollowers.stream()
                .filter(Objects::nonNull)
                .filter(id -> !id.equals(currentUserId))
                .filter(id -> !iFollow.contains(id))
                .distinct()
                .toList();

        if (secondDegreeUsers.isEmpty())
            return Collections.emptyList();

        List<Post> posts = postRepository.findByUserIdsAndNotDeleted(
                secondDegreeUsers,
                PageRequest.of(0, limit));

        return limitPostsPerUser(posts, MAX_POSTS_FROM_SAME_USER, limit);
    }

    private List<Post> getRandomPosts(UUID currentUserId, int limit) {

        if (limit <= 0) {
            return Collections.emptyList();
        }

        List<UUID> ids = new ArrayList<>();
        ids.add(currentUserId);

        List<Post> posts = postRepository.findRandomPostsExcludingUsers(
                ids,
                PageRequest.of(0, limit));

        return limitPostsPerUser(posts, MAX_POSTS_FROM_SAME_USER, limit);
    }

    private Set<UUID> findLikedPostIds(UUID profileId, List<Post> posts) {
        if (posts.isEmpty()) {
            return Collections.emptySet();
        }

        List<UUID> postIds = posts.stream().map(Post::getId).toList();
        return likeRepository.findLikedPostIds(profileId, postIds);
    }

    private List<Post> applyFinalSorting(List<Post> posts) {
        Map<UUID, Long> authorPostCounts = posts.stream()
                .filter(post -> post.getProfile() != null && post.getProfile().getId() != null)
                .collect(Collectors.groupingBy(post -> post.getProfile().getId(), Collectors.counting()));

        return posts.stream()
                .sorted(Comparator.comparing((Post post) -> calculatePostScore(post, authorPostCounts)).reversed())
                .collect(Collectors.toList());
    }

    private double calculatePostScore(Post post, Map<UUID, Long> authorPostCounts) {
        double score = 0;

        if (post.getCreatedAt() != null) {
            long hoursOld = Duration.between(post.getCreatedAt(), Instant.now()).toHours();

            if (hoursOld <= 24) {
                score += (24 - hoursOld) * 0.5;
            }
        }

        if (post.getLikesCount() != null) {
            score += Math.log(post.getLikesCount() + 1) * 0.3;
        }

        if (post.getProfile() != null && post.getProfile().getId() != null) {
            long authorCount = authorPostCounts.getOrDefault(post.getProfile().getId(), 0L);
            score += 1.0 / (authorCount + 1);
        }

        // ruído controlado para dar variedade
        score += Math.random() * 1.5;

        return score;
    }

    private List<Post> limitPostsPerUser(List<Post> posts, int maxPerUser, int totalLimit) {

        Map<UUID, Integer> userPostCount = new HashMap<>();
        List<Post> result = new ArrayList<>();

        for (Post post : posts) {
            if (result.size() >= totalLimit) {
                break;
            }

            if (post.getProfile() == null || post.getProfile().getId() == null) {
                result.add(post);
                continue;
            }

            UUID userId = post.getProfile().getId();
            int count = userPostCount.getOrDefault(userId, 0);

            if (count < maxPerUser) {
                result.add(post);
                userPostCount.put(userId, count + 1);
            }
        }

        return result;
    }

    private int calculateQuota(int pageSize, double ratio) {
        if (pageSize <= 0) {
            return 0;
        }

        return (int) Math.floor(pageSize * ratio);
    }

    private int calculateCandidatePoolSize(int pageSize, int targetCount, int page) {
        if (targetCount <= 0) {
            return 0;
        }

        return targetCount * 4;
    }

    private List<Post> sliceForPage(List<Post> posts, int page, int targetCount) {
        if (posts.isEmpty() || targetCount <= 0) {
            return Collections.emptyList();
        }

        List<Post> shuffled = new ArrayList<>(posts);
        Collections.shuffle(shuffled);

        int start = page * targetCount;
        if (start >= shuffled.size()) {
            return Collections.emptyList();
        }

        int end = Math.min(start + targetCount, shuffled.size());
        return new ArrayList<>(shuffled.subList(start, end));
    }

    private int addCategoryPosts(List<Post> target, List<Post> source, int maxCount, Set<UUID> usedPostIds,
            UUID currentProfileId) {

        if (maxCount <= 0 || source.isEmpty()) {
            return 0;
        }

        int added = 0;

        for (Post post : source) {
            Profile author = post.getProfile();

            if (author != null && author.getId() != null && author.getId().equals(currentProfileId)) {
                continue;
            }

            if (usedPostIds.add(post.getId())) {
                target.add(post);
                added++;
            }

            if (added >= maxCount) {
                break;
            }
        }

        return added;
    }
}
