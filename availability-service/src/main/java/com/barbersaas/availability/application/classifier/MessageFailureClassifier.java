package com.barbersaas.availability.application.classifier;

import com.barbersaas.availability.domain.exception.SlotValidationException;
import com.barbersaas.availability.domain.exception.messaging.NonRetryableMessageException;
import com.barbersaas.availability.domain.exception.messaging.RetryableMessageException;
import org.springframework.stereotype.Component;

@Component
public class MessageFailureClassifier {
  public RuntimeException classify(RuntimeException exception) {
    if (exception instanceof SlotValidationException) {
      return new NonRetryableMessageException("Non-retryable message failure", exception);
    }

    if (exception instanceof IllegalArgumentException) {
      return new NonRetryableMessageException("Non-retryable message failure", exception);
    }

    return new RetryableMessageException("Retryable message failure", exception);
  }
}
