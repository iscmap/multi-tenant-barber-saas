package com.barbersaas.availability.adapters.in.web.exception;

import com.barbersaas.shared.api.problem.ApiProblem;
import com.barbersaas.shared.logging.CorrelationIdHolder;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiProblem> handleConstraintViolation(
      ConstraintViolationException exception) {

    String detail =
        exception.getConstraintViolations().stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
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

  @ExceptionHandler(HandlerMethodValidationException.class)
  public ResponseEntity<ApiProblem> handleMethodValidation(
      HandlerMethodValidationException exception) {

    ApiProblem problem =
        ApiProblem.builder()
            .type("https://example.com/problems/validation-error")
            .title("Validation error")
            .status(HttpStatus.BAD_REQUEST.value())
            .detail("Invalid request parameter")
            .instance(null)
            .correlationId(CorrelationIdHolder.get())
            .timestamp(OffsetDateTime.now())
            .build();

    return ResponseEntity.badRequest().body(problem);
  }
}
