package com.barbersaas.shared.messaging.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "barbersaas.messaging")
public class MessagingProperties {

  private String bookingEventsTopic;
  private String bookingEventsTopicArn;
  private String bookingCreatedQueue;
  private String availabilityEventsTopic;
  private String availabilityEventsTopicArn;
  private String availabilityDecidedQueue;
  private String bookingEventsDlq;
  private String availabilityEventsDlq;

  private String bookingCreatedKafkaTopic;
  private String availabilityKafkaConsumerGroup;
  private String kafkaBootstrapServers;
  private boolean kafkaListenerAutoStartup = true;
}
