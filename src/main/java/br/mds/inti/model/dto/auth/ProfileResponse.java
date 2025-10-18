package br.mds.inti.model.dto.auth;

import java.time.Instant;
import java.util.UUID;

import br.mds.inti.model.enums.ProfileType;

public record ProfileResponse(
        UUID id,
        String username,
        String name,
        String email,
        ProfileType type,
        Instant createdAt

) {

}
