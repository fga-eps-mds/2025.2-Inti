package br.mds.inti.auth.dto;

import br.mds.inti.models.ENUM.ProfileType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(@NotBlank String name, @NotBlank String username, @NotBlank @Email String email,
        @NotBlank String password, ProfileType type) {

}
