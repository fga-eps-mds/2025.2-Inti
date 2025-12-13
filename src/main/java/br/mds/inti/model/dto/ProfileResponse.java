package br.mds.inti.model.dto;

import br.mds.inti.model.enums.ProfileType;

import java.util.List;
import java.util.UUID;

public record ProfileResponse(UUID id, String name, String username, String publicEmail, String phone,
                ProfileType type, String profile_picture_url, String bio,
                Integer followersCount, Integer followingCount, Long totalPosts,
                Boolean isFollowing, List<PostResponse> posts) {

}
