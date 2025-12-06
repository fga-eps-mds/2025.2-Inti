package br.mds.inti.model.dto;

import java.util.List;
import java.util.UUID;

public record PostDetailResponse(
                UUID id,
                String imageUrl,
                String description,
                Integer likesCount,
                String createdAt,
                UserSummaryResponse author,
                List<UserSummaryResponse> likedBy,
                boolean liked) {
}
