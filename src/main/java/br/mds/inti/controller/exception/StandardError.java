package br.mds.inti.controller.exception;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StandardError {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "uuuu-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant timeStamp;

    private Integer status;

    private String error;

    private String msg;

    private String path;

}
