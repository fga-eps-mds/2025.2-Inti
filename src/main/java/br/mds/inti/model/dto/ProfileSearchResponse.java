package br.mds.inti.model.dto;

import java.util.UUID;

public record ProfileSearchResponse(
        UUID id,
        String username,
        String profilePictureUrl) {
}
