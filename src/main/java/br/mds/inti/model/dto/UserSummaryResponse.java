package br.mds.inti.model.dto;

import java.util.UUID;

public record UserSummaryResponse(
        UUID id,
        String name,
        String username,
        String profilePictureUrl) {
}
