package com.barbersaas.availability.adapters.out.deduplication.dynamodb;

import com.barbersaas.availability.application.port.out.deduplication.LoadProcessedBookingEventPort;
import com.barbersaas.availability.application.port.out.deduplication.SaveProcessedBookingEventPort;
import com.barbersaas.availability.domain.model.event.ProcessedBookingEvent;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

@Repository
@Primary
public class DynamoDbProcessedBookingEventRepository
    implements LoadProcessedBookingEventPort, SaveProcessedBookingEventPort {

  private static final String TABLE_NAME = "processed_booking_events";

  private static final String BOOKING_ID_EVENT_TYPE = "bookingIdEventType";
  private static final String BOOKING_ID = "bookingId";
  private static final String EVENT_TYPE = "eventType";
  private static final String PROCESSED_AT = "processedAt";

  private final DynamoDbClient dynamoDbClient;

  public DynamoDbProcessedBookingEventRepository(DynamoDbClient dynamoDbClient) {
    this.dynamoDbClient = dynamoDbClient;
  }

  @Override
  public Optional<ProcessedBookingEvent> loadByBookingIdAndEventType(
      String bookingId, String eventType) {

    GetItemRequest request =
        GetItemRequest.builder()
            .tableName(TABLE_NAME)
            .key(
                Map.of(
                    BOOKING_ID_EVENT_TYPE,
                    AttributeValue.builder().s(key(bookingId, eventType)).build()))
            .consistentRead(true)
            .build();

    Map<String, AttributeValue> item = dynamoDbClient.getItem(request).item();

    if (item == null || item.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(
        ProcessedBookingEvent.builder()
            .bookingId(item.get(BOOKING_ID).s())
            .eventType(item.get(EVENT_TYPE).s())
            .processedAt(LocalDateTime.parse(item.get(PROCESSED_AT).s()))
            .build());
  }

  @Override
  public ProcessedBookingEvent save(ProcessedBookingEvent processedBookingEvent) {

    Map<String, AttributeValue> item =
        Map.of(
            BOOKING_ID_EVENT_TYPE,
            AttributeValue.builder()
                .s(key(processedBookingEvent.getBookingId(), processedBookingEvent.getEventType()))
                .build(),
            BOOKING_ID,
            AttributeValue.builder().s(processedBookingEvent.getBookingId()).build(),
            EVENT_TYPE,
            AttributeValue.builder().s(processedBookingEvent.getEventType()).build(),
            PROCESSED_AT,
            AttributeValue.builder().s(processedBookingEvent.getProcessedAt().toString()).build());

    PutItemRequest request = PutItemRequest.builder().tableName(TABLE_NAME).item(item).build();

    dynamoDbClient.putItem(request);

    return processedBookingEvent;
  }

  private String key(String bookingId, String eventType) {

    return bookingId + "|" + eventType;
  }
}
