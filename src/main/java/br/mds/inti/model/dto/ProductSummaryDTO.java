package br.mds.inti.model.dto;

import java.util.UUID;

public record ProductSummaryDTO(
        UUID id,
        String title,
        String imgLink,
        Double price,
        String description
) {}
