package br.mds.inti.model.dto;

import java.util.List;
import java.util.UUID;

public record ProfileResponse(UUID id, String name, String username, String publicEmail, String phone,
        String profile_picture_url, String bio,
        Integer followersCount, Integer followingCount, List<PostResponse> posts) {

}
