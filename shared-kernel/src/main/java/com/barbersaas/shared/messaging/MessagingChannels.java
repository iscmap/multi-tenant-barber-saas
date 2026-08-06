package com.barbersaas.shared.messaging;

public final class MessagingChannels {

  public static final String BOOKING_EVENTS_TOPIC = "booking-events";
  public static final String BOOKING_CREATED_QUEUE = "booking-created-queue";
  public static final String AVAILABILITY_EVENTS_TOPIC = "availability-events";
  public static final String AVAILABILITY_DECIDED_QUEUE = "availability-decided-queue";
  public static final String BOOKING_EVENTS_DLQ = "booking-events-dlq";
  public static final String AVAILABILITY_EVENTS_DLQ = "availability-events-dlq";
  public static final String BOOKING_CREATED_EVENT_TYPE = "BookingCreated";
  public static final String AVAILABILITY_DECIDED_EVENT_TYPE = "AvailabilityDecided";

  public static final String BOOKING_CREATED_KAFKA_TOPIC = "booking-created-kafka";
  public static final String AVAILABILITY_KAFKA_CONSUMER_GROUP =
      "availability-booking-created-group";

  private MessagingChannels() {}
}
