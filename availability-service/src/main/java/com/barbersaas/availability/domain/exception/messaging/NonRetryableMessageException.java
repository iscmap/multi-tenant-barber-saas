package com.barbersaas.availability.domain.exception.messaging;

public class NonRetryableMessageException extends RuntimeException {
  public NonRetryableMessageException(String message, Throwable cause) {
    super(message, cause);
  }
}
