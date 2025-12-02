package br.mds.inti.model.dto;

import java.math.BigDecimal;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;

public record CreateProductDTO(
        @NotNull String title,
        @NotNull String description,
        @NotNull BigDecimal price,
        MultipartFile image) {
}
