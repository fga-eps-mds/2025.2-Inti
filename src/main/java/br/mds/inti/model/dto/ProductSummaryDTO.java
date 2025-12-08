package br.mds.inti.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO resumido de produto para listagens e visualizações em grade.
 * 
 * Contém informações essenciais do produto sem dados completos,
 * otimizado para respostas de API com menor consumo de dados.
 */
public record ProductSummaryDTO(

        @NotNull(message = "ID do produto não pode ser nulo")
        UUID id,

        @NotBlank(message = "Título do produto é obrigatório")
        String title,

        @NotBlank(message = "Link da imagem é obrigatório")
        String imgLink,

        @NotNull(message = "Preço não pode ser nulo")
        @DecimalMin(value = "0.0", message = "Preço não pode ser negativo")
        BigDecimal price,

        @NotBlank(message = "Descrição curta é obrigatória")
        String shortDescription) {
}