package br.mds.inti.controller.exception;

import br.mds.inti.model.dto.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.mds.inti.model.dto.ErrorResponse;
import br.mds.inti.service.exception.ProfileNotFoundException;
import br.mds.inti.service.exception.EntityNotFoundException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException e) {
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ProfileNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProfileNotFound(ProfileNotFoundException e) {
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
        ErrorResponse error = new ErrorResponse(HttpStatus.FORBIDDEN.value(), "Acesso negado");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<StandardError> handleResponseStatusException(ResponseStatusException ex) {

        StandardError standardError = new StandardError(Instant.now(), ex.getStatusCode().value(), null, ex.getReason(), null);

        return ResponseEntity.status(ex.getStatusCode()).body(standardError);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<StandardError> handleResponseStatusException(RuntimeException ex) {

        StandardError standardError = new StandardError(Instant.now(), HttpStatus.BAD_REQUEST.value(), null, ex.getMessage(), null);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(standardError);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map((error) -> error.getField() + " " + error.getDefaultMessage())
                .toList();

        var errorResponseDTO = ErrorResponseDTO.builder()
                .errors(errors)
                .build();

        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;

        return ResponseEntity.status(status).body(errorResponseDTO);
    }
}