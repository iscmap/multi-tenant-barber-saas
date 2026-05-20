package com.barbersaas.booking.application.port.out.messaging;

public interface PublishMessagePort {

  void publish(String topicName, String payload);
}
