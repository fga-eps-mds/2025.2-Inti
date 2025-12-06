package br.mds.inti.model.dto.auth;

import br.mds.inti.model.enums.ProfileType;

public record LoginResponse(
        String jwt,
        String username,
        String name,
        String email,
        ProfileType type) {
}
