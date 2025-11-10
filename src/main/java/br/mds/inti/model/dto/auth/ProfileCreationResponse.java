package br.mds.inti.model.dto.auth;

import java.time.Instant;
import java.util.UUID;

import br.mds.inti.model.enums.ProfileType;

public record ProfileCreationResponse(
        UUID id,
        String username,
        String name,
        String email,
        String jwt,
        ProfileType type,
        Instant createdAt

) {

}
