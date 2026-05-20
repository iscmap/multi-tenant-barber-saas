package com.barbersaas.availability.domain.exception.messaging;

public class RetryableMessageException extends RuntimeException {
  public RetryableMessageException(String message, Throwable cause) {
    super(message, cause);
  }
}
