package com.barbersaas.availability.adapters.in.messaging.sqs;

import com.barbersaas.availability.application.classifier.MessageFailureClassifier;
import com.barbersaas.availability.application.port.in.event.ConsumeBookingCreatedUseCase;
import com.barbersaas.availability.domain.exception.messaging.NonRetryableMessageException;
import com.barbersaas.availability.domain.exception.messaging.RetryableMessageException;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BookingCreatedSqsListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(BookingCreatedSqsListener.class);

  private final ConsumeBookingCreatedUseCase consumeBookingCreatedUseCase;
  private final MessageFailureClassifier messageFailureClassifier;

  public BookingCreatedSqsListener(
      ConsumeBookingCreatedUseCase consumeBookingCreatedUseCase,
      MessageFailureClassifier messageFailureClassifier) {
    this.consumeBookingCreatedUseCase = consumeBookingCreatedUseCase;
    this.messageFailureClassifier = messageFailureClassifier;
  }

  @SqsListener("${barbersaas.messaging.booking-created-queue}")
  public void listen(String payload) {
    try {
      consumeBookingCreatedUseCase.consume(payload);
    } catch (RuntimeException exception) {
      RuntimeException classifiedException = messageFailureClassifier.classify(exception);

      if (classifiedException instanceof NonRetryableMessageException) {
        LOGGER.error(
            "booking_created_message_non_retryable payload={} reason={}",
            payload,
            exception.getMessage(),
            exception);
        return;
      }

      if (classifiedException instanceof RetryableMessageException) {
        LOGGER.error(
            "booking_created_message_retryable payload={} reason={}",
            payload,
            exception.getMessage(),
            exception);
        throw classifiedException;
      }

      throw classifiedException;
    }
  }
}
