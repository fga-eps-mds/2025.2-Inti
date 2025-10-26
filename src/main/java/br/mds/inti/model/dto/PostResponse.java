package br.mds.inti.model.dto;

import java.util.UUID;

public record PostResponse(
                UUID id,
                String imgLink,
                String description,
                Integer likesCount,

                String createdAt) {
}