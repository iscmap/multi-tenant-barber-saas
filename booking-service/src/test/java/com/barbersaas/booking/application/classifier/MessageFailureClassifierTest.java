package com.barbersaas.booking.application.classifier;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.barbersaas.booking.domain.exception.messaging.NonRetryableMessageException;
import com.barbersaas.booking.domain.exception.messaging.RetryableMessageException;
import org.junit.jupiter.api.Test;

class MessageFailureClassifierTest {

  private final MessageFailureClassifier classifier = new MessageFailureClassifier();

  @Test
  void shouldClassifyIllegalArgumentAsNonRetryable() {
    RuntimeException classified =
        classifier.classify(new IllegalArgumentException("Booking not found"));

    assertInstanceOf(NonRetryableMessageException.class, classified);
  }

  @Test
  void shouldClassifyIllegalStateAsNonRetryable() {
    RuntimeException classified =
        classifier.classify(new IllegalStateException("Invalid state transition"));

    assertInstanceOf(NonRetryableMessageException.class, classified);
  }

  @Test
  void shouldClassifyOtherExceptionAsRetryable() {
    RuntimeException classified =
        classifier.classify(new RuntimeException("Temporary database issue"));

    assertInstanceOf(RetryableMessageException.class, classified);
  }
}
