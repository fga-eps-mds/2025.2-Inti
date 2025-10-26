package br.mds.inti.model.dto.auth;

public record ProfileResponse(String name, String username, String profile_picture_url, String bio,
        Integer followersCount, Integer followingCount) {

}
