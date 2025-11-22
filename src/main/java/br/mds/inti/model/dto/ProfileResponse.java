package br.mds.inti.model.dto;

import java.util.List;

public record ProfileResponse(String name, String username, String publicEmail, String phone,
        String profile_picture_url, String bio,
        Integer followersCount, Integer followingCount, List<PostResponse> posts) {

}
