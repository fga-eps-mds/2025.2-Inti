package br.mds.inti.model.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EventDetailResponse(
                UUID id,
                String title,
                String imageUrl,
                Instant eventTime,
                String description,
                LocalAddress address,
                BigDecimal latitude,
                BigDecimal longitude,
                Instant finishedAt,
                boolean registered,
                UUID organizerId,
                String organizerUsername,
                String organizerProfilePictureUrl) {
}