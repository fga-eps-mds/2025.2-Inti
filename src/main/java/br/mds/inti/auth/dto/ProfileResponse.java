package br.mds.inti.auth.dto;

import java.time.Instant;
import java.util.UUID;

import br.mds.inti.models.ENUM.ProfileType;

public record ProfileResponse(
        UUID id,
        String username,
        String name,
        String email,
        ProfileType type,
        Instant createdAt

) {

}
