package com.barbersaas.booking.application.classifier;

import com.barbersaas.booking.domain.exception.messaging.NonRetryableMessageException;
import com.barbersaas.booking.domain.exception.messaging.RetryableMessageException;
import org.springframework.stereotype.Component;

@Component
public class MessageFailureClassifier {

  public RuntimeException classify(RuntimeException exception) {
    if (exception instanceof IllegalArgumentException) {
      return new NonRetryableMessageException("Non-retryable message failure", exception);
    }

    if (exception instanceof IllegalStateException) {
      return new NonRetryableMessageException("Non-retryable message failure", exception);
    }

    return new RetryableMessageException("Retryable message failure", exception);
  }
}
