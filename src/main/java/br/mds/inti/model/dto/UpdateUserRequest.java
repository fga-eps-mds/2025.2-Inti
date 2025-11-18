package br.mds.inti.model.dto;

import org.springframework.web.multipart.MultipartFile;

public record UpdateUserRequest(
        String name,
        String username,
        String userBio,
        MultipartFile profilePicture) {
}
