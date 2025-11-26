package br.mds.inti.model.dto;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.UUID;

public record EventListResponse(String title, String imageUrl, LocalDateTime data, UUID id) {

}
