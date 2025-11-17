package br.mds.inti.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record EventRequestDTO (

    @NotBlank // String não nula e não vazia
    String title,

    @NotNull // não nulo
    @JsonProperty("event_time")
    Instant eventTime,

    @NotNull
    String description,

    MultipartFile image,

    @NotNull
    @JsonProperty("street_address")
    String streetAddress,

    @NotNull
    @JsonProperty("administrative_region")
    String administrativeRegion,

    @NotNull
    String city,

    @NotNull
    String state,

    @NotNull
    @JsonProperty("reference_point")
    String referencePoint,

    @NotNull
    BigDecimal latitude,

    @NotNull
    BigDecimal longitude
){}