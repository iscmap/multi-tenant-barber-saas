package com.barbersaas.booking.adapters.in.messaging.sqs;

import com.barbersaas.booking.application.classifier.MessageFailureClassifier;
import com.barbersaas.booking.application.port.in.event.ConsumeAvailabilityDecidedUseCase;
import com.barbersaas.booking.domain.exception.messaging.NonRetryableMessageException;
import com.barbersaas.booking.domain.exception.messaging.RetryableMessageException;
import io.awspring.cloud.sqs.annotation.SnsNotificationMessage;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AvailabilityDecidedSqsListener {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(AvailabilityDecidedSqsListener.class);

  private final ConsumeAvailabilityDecidedUseCase consumeAvailabilityDecidedUseCase;
  private final MessageFailureClassifier messageFailureClassifier;

  public AvailabilityDecidedSqsListener(
      ConsumeAvailabilityDecidedUseCase consumeAvailabilityDecidedUseCase,
      MessageFailureClassifier messageFailureClassifier) {
    this.consumeAvailabilityDecidedUseCase = consumeAvailabilityDecidedUseCase;
    this.messageFailureClassifier = messageFailureClassifier;
  }

  @SqsListener("${barbersaas.messaging.availability-decided-queue}")
  public void listen(@SnsNotificationMessage String payload) {
    try {
      consumeAvailabilityDecidedUseCase.consume(payload);
    } catch (RuntimeException exception) {
      RuntimeException classifiedException = messageFailureClassifier.classify(exception);

      if (classifiedException instanceof NonRetryableMessageException) {
        LOGGER.error(
            "availability_decided_message_non_retryable payload={} reason={}",
            payload,
            exception.getMessage(),
            exception);
        return;
      }

      if (classifiedException instanceof RetryableMessageException) {
        LOGGER.error(
            "availability_decided_message_retryable payload={} reason={}",
            payload,
            exception.getMessage(),
            exception);
        throw classifiedException;
      }

      throw classifiedException;
    }
  }
}
