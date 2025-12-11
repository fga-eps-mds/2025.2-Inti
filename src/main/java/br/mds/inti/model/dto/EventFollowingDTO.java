package br.mds.inti.model.dto;

import java.util.UUID;

public record EventFollowingDTO(
                UUID profileId,
                String username,
                String profilePictureUrl) {
}
