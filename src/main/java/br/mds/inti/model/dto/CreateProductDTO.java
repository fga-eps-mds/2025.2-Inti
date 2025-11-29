package br.mds.inti.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateProductDTO {

    @NotBlank(message = "O título é obrigatório.")
    @Size(min = 3, message = "O título deve ter no mínimo 3 caracteres.")
    private String title;

    @NotBlank(message = "A descrição é obrigatória.")
    private String description;

    @DecimalMin(value = "0.00", inclusive = true, message = "O preço não pode ser negativo.")
    private BigDecimal price;

    @NotBlank(message = "O link da imagem é obrigatório.")
    private String imgLink; // Assumindo que a validação de URL será feita no service ou por outra anotação se houver

    @NotNull(message = "As tags são obrigatórias.")
    private List<String> tags;

    private String contactInfo;

    @NotBlank(message = "A visibilidade é obrigatória.")
    private String visibility; // Ex: PUBLIC, PRIVATE
}
