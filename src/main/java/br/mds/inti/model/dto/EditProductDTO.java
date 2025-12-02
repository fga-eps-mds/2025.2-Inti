package br.mds.inti.model.dto;

import java.math.BigDecimal;

import org.springframework.web.multipart.MultipartFile;

public record EditProductDTO(String title,
        String description,
        BigDecimal price,
        MultipartFile image) {
}