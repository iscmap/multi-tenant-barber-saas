package com.barbersaas.shared.events.parser;

public interface EventJsonParser {

  <T> T parse(String payload, Class<T> eventType);

  String toJson(Object event);
}
