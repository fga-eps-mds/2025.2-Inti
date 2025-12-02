package br.mds.inti.model.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MyEvent(UUID id, String title, String imageUrl, LocalDateTime data) {

}
