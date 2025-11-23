package br.mds.inti.model.dto;

import java.util.UUID;

// record é uma classe que tem getter e setter
// DTO = Data Transfer Object, um objeto que vai ser usado pra quase tudo, menos banco de dados
public record EventResponseDTO(
        UUID id,
        String message
) {
}
