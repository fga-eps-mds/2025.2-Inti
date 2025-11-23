package br.mds.inti.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ErrorResponseDTO {
    private List<String> errors;
}
