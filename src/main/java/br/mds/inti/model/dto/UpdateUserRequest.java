package br.mds.inti.model.dto;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRequest (
    String name,
    String username,
    String userBio,
    MultipartFile profilePicture
) {
}
