package com.barbersaas.shared.events.parser;

import com.barbersaas.shared.events.envelope.EventEnvelope;

public interface EventJsonParser {

  <T> T parse(String json, Class<T> type);

  String toJson(Object object);

  <T> EventEnvelope<T> parseEventEnvelope(String json, Class<T> payloadType);
}
