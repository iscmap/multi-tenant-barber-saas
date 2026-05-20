package com.barbersaas.shared.events.parser;

import com.barbersaas.shared.events.envelope.EventEnvelope;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JacksonEventJsonParser implements EventJsonParser {

  private final ObjectMapper objectMapper;

  public JacksonEventJsonParser() {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JavaTimeModule());
  }

  @Override
  public <T> T parse(String payload, Class<T> eventType) {
    try {
      return objectMapper.readValue(payload, eventType);
    } catch (JsonProcessingException ex) {
      throw new IllegalArgumentException("Failed to parse event payload", ex);
    }
  }

  @Override
  public String toJson(Object event) {
    try {
      return objectMapper.writeValueAsString(event);
    } catch (JsonProcessingException ex) {
      throw new IllegalArgumentException("Failed to parse event payload", ex);
    }
  }

  @Override
  public <T> EventEnvelope<T> parseEventEnvelope(String json, Class<T> payloadType) {
    try {
      JavaType envelopeType =
          objectMapper.getTypeFactory().constructParametricType(EventEnvelope.class, payloadType);

      return objectMapper.readValue(json, envelopeType);
    } catch (Exception exception) {
      throw new IllegalArgumentException("Failed to parse event envelope json", exception);
    }
  }
}
