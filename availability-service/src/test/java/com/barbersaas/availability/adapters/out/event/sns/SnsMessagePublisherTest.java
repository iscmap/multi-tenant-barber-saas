package com.barbersaas.availability.adapters.out.event.sns;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

class SnsMessagePublisherTest {

  private final SnsClient snsClient = mock(SnsClient.class);

  @Test
  void shouldCreateTopicAndPublishMessage() {
    when(snsClient.createTopic(any(CreateTopicRequest.class)))
        .thenReturn(
            CreateTopicResponse.builder()
                .topicArn("arn:aws:sns:us-east-1:000000000000:availability-events")
                .build());

    when(snsClient.publish(any(PublishRequest.class)))
        .thenReturn(PublishResponse.builder().messageId("msg-1").build());

    SnsMessagePublisher publisher = new SnsMessagePublisher(snsClient);

    publisher.publish("availability-events", "{\"hello\":\"world\"}");

    verify(snsClient).createTopic(any(CreateTopicRequest.class));
    verify(snsClient).publish(any(PublishRequest.class));
  }
}
