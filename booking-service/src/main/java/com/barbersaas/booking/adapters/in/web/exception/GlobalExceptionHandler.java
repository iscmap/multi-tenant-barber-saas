package com.barbersaas.booking.adapters.in.web.exception;

import com.barbersaas.shared.api.problem.ApiProblem;
import com.barbersaas.shared.logging.CorrelationIdHolder;
import java.time.OffsetDateTime;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiProblem> handleValidationError(
      MethodArgumentNotValidException exception) {
    String detail =
        exception.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));

    ApiProblem problem =
        ApiProblem.builder()
            .type("https://example.com/problems/validation-error")
            .title("Validation error")
            .status(HttpStatus.BAD_REQUEST.value())
            .detail(detail)
            .instance(null)
            .correlationId(CorrelationIdHolder.get())
            .timestamp(OffsetDateTime.now())
            .build();

    return ResponseEntity.badRequest().body(problem);
  }

  @ExceptionHandler(BindException.class)
  public ResponseEntity<ApiProblem> handleBindError(BindException exception) {
    String detail =
        exception.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));

    ApiProblem problem =
        ApiProblem.builder()
            .type("https://example.com/problems/bind-error")
            .title("Bind error")
            .status(HttpStatus.BAD_REQUEST.value())
            .detail(detail)
            .instance(null)
            .correlationId(CorrelationIdHolder.get())
            .timestamp(OffsetDateTime.now())
            .build();

    return ResponseEntity.badRequest().body(problem);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiProblem> handleGenericError(Exception exception) {
    ApiProblem problem =
        ApiProblem.builder()
            .type("https://example.com/problems/internal-error")
            .title("Internal server error")
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .detail("Unexpected error")
            .instance(null)
            .correlationId(CorrelationIdHolder.get())
            .timestamp(OffsetDateTime.now())
            .build();

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
  }
}
