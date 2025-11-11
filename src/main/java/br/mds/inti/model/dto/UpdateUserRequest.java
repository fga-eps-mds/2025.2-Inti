package br.mds.inti.model.dto.user;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRequest (
    @NotBlank String name,
    @NotBlank String username,
    @NotBlank String userBio,
    @NotNull MultipartFile profilePicture
) {
}
