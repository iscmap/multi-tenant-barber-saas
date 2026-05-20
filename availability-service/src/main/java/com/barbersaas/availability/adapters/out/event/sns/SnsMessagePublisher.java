package com.barbersaas.availability.adapters.out.event.sns;

import com.barbersaas.availability.application.port.out.messaging.PublishMessagePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Component
public class SnsMessagePublisher implements PublishMessagePort {

  private static final Logger LOGGER = LoggerFactory.getLogger(SnsMessagePublisher.class);

  private final SnsClient snsClient;

  public SnsMessagePublisher(SnsClient snsClient) {
    this.snsClient = snsClient;
  }

  @Override
  public void publish(String topicName, String payload) {
    String topicArn =
        snsClient.createTopic(CreateTopicRequest.builder().name(topicName).build()).topicArn();

    snsClient.publish(PublishRequest.builder().topicArn(topicArn).message(payload).build());

    LOGGER.info("sns_message_published topicName={} topicArn={}", topicName, topicArn);
  }
}
