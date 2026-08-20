package com.barbersaas.availability.adapters.out.event.sns;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

class SnsMessagePublisherTest {

  private static final String TOPIC_ARN = "arn:aws:sns:us-east-1:000000000000:availability-events";

  private final SnsClient snsClient = mock(SnsClient.class);

  @Test
  void shouldPublishMessageToExistingTopicArn() {
    when(snsClient.publish(any(PublishRequest.class)))
        .thenReturn(PublishResponse.builder().messageId("msg-1").build());

    SnsMessagePublisher publisher = new SnsMessagePublisher(snsClient);

    publisher.publish(TOPIC_ARN, "{\"hello\":\"world\"}");

    verify(snsClient)
        .publish(
            PublishRequest.builder().topicArn(TOPIC_ARN).message("{\"hello\":\"world\"}").build());
  }
}
