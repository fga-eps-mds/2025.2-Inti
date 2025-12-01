package br.mds.inti.model.dto;

import java.time.Instant;
import java.util.UUID;

public record EventParticipantResponse(
    UUID eventid,
    UUID profileid,
    Instant createdAt
) {
}
