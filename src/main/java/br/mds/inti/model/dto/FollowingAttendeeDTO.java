package br.mds.inti.model.dto;

import java.util.UUID;

public record FollowingAttendeeDTO(
                UUID profileId,
                String username,
                String profilePictureUrl) {
}
