package com.barbersaas.availability.domain.exception;

public class SlotValidationException extends RuntimeException {

  public SlotValidationException(String message) {
    super(message);
  }
}
