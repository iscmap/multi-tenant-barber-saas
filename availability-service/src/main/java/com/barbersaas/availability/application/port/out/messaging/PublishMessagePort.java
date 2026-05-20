package com.barbersaas.availability.application.port.out.messaging;

public interface PublishMessagePort {

  void publish(String topicName, String payload);
}
