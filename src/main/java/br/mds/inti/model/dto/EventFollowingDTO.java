package br.mds.inti.model.dto;

import lombok.AllArgsConstructor;

import java.util.UUID;

public record EventFollowingDTO(
        UUID profileId,
        String username,
        String profilePictureUrl
) {
}
