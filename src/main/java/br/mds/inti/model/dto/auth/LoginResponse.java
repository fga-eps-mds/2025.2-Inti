package br.mds.inti.model.dto.auth;

import br.mds.inti.model.enums.ProfileType;
import java.util.UUID;

public record LoginResponse(
                UUID id,
                String jwt,
                String username,
                String name,
                String email,
                ProfileType type) {
}
