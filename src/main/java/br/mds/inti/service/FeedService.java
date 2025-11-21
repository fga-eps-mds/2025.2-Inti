package br.mds.inti.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import br.mds.inti.model.entity.Post;
import br.mds.inti.model.entity.Profile;
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

    public List<Post> generateFeed(Profile currentProfile, int pageSize) {

        List<Post> feed = new ArrayList<>();

        // post de quem voce segue
        List<UUID> followedIds = followRepository.findFollowedUserIds(currentProfile.getId());

        if (!followedIds.isEmpty()) {
            int followedCount = (int) (pageSize * FOLLOWED_RATIO);
            List<Post> followedPosts = getFollowedUsersPosts(followedIds, followedCount);
            feed.addAll(followedPosts);
        }

        // post de seguidores de seguidores
        int secondDegreeCount = (int) (pageSize * SECOND_DEGREE_RATIO);
        List<Post> secondDegreePosts = getSecondDegreePosts(currentProfile.getId(),
                secondDegreeCount);
        feed.addAll(secondDegreePosts);

        int organizationCount = (int) (pageSize * ORGANIZATION_RATIO);
        List<Post> organizationPosts = getOrganizationPost(organizationCount);
        feed.addAll(organizationPosts);

        int remaining = pageSize - feed.size();
        if (remaining > 0) {

            List<Post> randomPosts = getRandomPosts(currentProfile.getId(), remaining);
            feed.addAll(randomPosts);

        }

        return applyFinalSorting(feed);
    }

    private List<Post> getFollowedUsersPosts(List<UUID> followedIds, int limit) {

        List<Post> posts = postRepository.findByUserIdsAndNotDeleted(followedIds, PageRequest.of(0, limit * 2));

        return limitPostsPerUser(posts, MAX_POSTS_FROM_SAME_USER, limit);
    }

    private List<Post> getOrganizationPost(int limit) {

        List<UUID> organizationsIds = profileRepository.findByOrganization("organization", PageRequest.of(0, 5));

        List<Post> organizationPosts = postRepository.findPostByOrganizationAndNotDeleted(organizationsIds,
                PageRequest.of(0, limit));

        if (organizationPosts.isEmpty()) {
            return Collections.emptyList();
        }

        return organizationPosts;

    }

    private List<Post> getSecondDegreePosts(UUID currentUserId, int limit) {
        // quem me segue
        List<UUID> myFollowers = followRepository.findFollowerIds(currentUserId);

        if (myFollowers.isEmpty()) {
            return Collections.emptyList();
        }

        // quem meus seguidores seguem
        List<UUID> followedByMyFollowers = followRepository.findFollowedByUsers(myFollowers);
        List<UUID> iAlreadyFollow = followRepository.findFollowedUserIds(currentUserId);

        List<UUID> secondDegreeUsers = followedByMyFollowers.stream().filter(userId -> !userId.equals(currentUserId))
                .filter(userId -> !iAlreadyFollow.contains(userId)).distinct().collect(Collectors.toList());

        if (secondDegreeUsers.isEmpty()) {
            return Collections.emptyList();
        }

        return postRepository.findByUserIdsAndNotDeleted(secondDegreeUsers, PageRequest.of(0, limit));
    }

    private List<Post> getRandomPosts(UUID currentUserId, int limit) {

        List<UUID> followedIds = followRepository.findFollowedUserIds(currentUserId);
        followedIds.add(currentUserId);

        return postRepository.findRecentPostsExcludingUsers(followedIds, PageRequest.of(0, limit));

    }

    private List<Post> applyFinalSorting(List<Post> posts) {
        return posts.stream().sorted(Comparator.comparing(this::calculatePostScore).reversed())
                .collect(Collectors.toList());
    }

    private double calculatePostScore(Post post) {
        double score = 0;

        // post das ultimas 24h tem bonus
        long hoursOld = Duration.between(post.getCreatedAt(), Instant.now()).toHours();

        if (hoursOld <= 24) {

            score += (24 - hoursOld) * 0.5; // bonus vai decrescendo
        }

        // engajamento
        if (post.getLikesCount() != null) {

            score += Math.log(post.getLikesCount() + 1) * 0.3; // log para não dominar
        }

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
