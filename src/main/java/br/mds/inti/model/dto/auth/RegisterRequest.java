package br.mds.inti.model.dto.auth;

import br.mds.inti.model.enums.ProfileType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(@NotBlank String name, @NotBlank String username, @NotBlank @Email String email,
                @NotBlank String password, ProfileType type) {

}
