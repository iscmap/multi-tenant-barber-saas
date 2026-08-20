package com.barbersaas.booking.adapters.out.event.sns;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

class SnsMessagePublisherTest {

  private static final String TOPIC_ARN = "arn:aws:sns:us-east-1:000000000000:booking-events";

  private final SnsClient snsClient = mock(SnsClient.class);

  @Test
  void shouldPublishMessageToExistingTopicArn() {
    when(snsClient.publish(any(PublishRequest.class)))
        .thenReturn(PublishResponse.builder().messageId("msg-1").build());

    SnsMessagePublisher publisher = new SnsMessagePublisher(snsClient);

    publisher.publish(TOPIC_ARN, "{\"hello\":\"world\"}");

    verify(snsClient)
        .publish(
            argThat(
                (PublishRequest request) ->
                    TOPIC_ARN.equals(request.topicArn())
                        && "{\"hello\":\"world\"}".equals(request.message())));
  }
}
