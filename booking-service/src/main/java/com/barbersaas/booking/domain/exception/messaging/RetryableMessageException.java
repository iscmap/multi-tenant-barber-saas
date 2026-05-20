package com.barbersaas.booking.domain.exception.messaging;

public class RetryableMessageException extends RuntimeException {

  public RetryableMessageException(String message, Throwable cause) {
    super(message, cause);
  }
}
