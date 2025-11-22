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
    Instant eventTime,

    @NotNull
    String description,

    MultipartFile image,

    @NotNull
    String streetAddress,

    @NotNull
    String administrativeRegion,

    @NotNull
    String city,

    @NotNull
    String state,

    @NotNull
    String referencePoint,

    @NotNull
    BigDecimal latitude,

    @NotNull
    BigDecimal longitude
){}