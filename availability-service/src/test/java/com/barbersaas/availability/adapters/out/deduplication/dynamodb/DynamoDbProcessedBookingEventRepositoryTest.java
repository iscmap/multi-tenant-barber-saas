package com.barbersaas.availability.adapters.out.deduplication.dynamodb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.barbersaas.availability.domain.model.event.ProcessedBookingEvent;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

class DynamoDbProcessedBookingEventRepositoryTest {

  @Mock private DynamoDbClient dynamoDbClient;

  private DynamoDbProcessedBookingEventRepository repository;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    repository = new DynamoDbProcessedBookingEventRepository(dynamoDbClient);
  }

  @Test
  void shouldLoadProcessedBookingEventWhenItExists() {

    when(dynamoDbClient.getItem(any(GetItemRequest.class)))
        .thenReturn(
            GetItemResponse.builder()
                .item(
                    Map.of(
                        "bookingIdEventType",
                        AttributeValue.builder().s("booking-1|BookingCreated").build(),
                        "bookingId",
                        AttributeValue.builder().s("booking-1").build(),
                        "eventType",
                        AttributeValue.builder().s("BookingCreated").build(),
                        "processedAt",
                        AttributeValue.builder().s("2026-08-13T19:00:00").build()))
                .build());

    Optional<ProcessedBookingEvent> result =
        repository.loadByBookingIdAndEventType("booking-1", "BookingCreated");

    assertTrue(result.isPresent());

    assertEquals("booking-1", result.get().getBookingId());

    assertEquals("BookingCreated", result.get().getEventType());

    assertEquals(LocalDateTime.of(2026, 8, 13, 19, 0), result.get().getProcessedAt());

    verify(dynamoDbClient).getItem(any(GetItemRequest.class));
  }

  @Test
  void shouldReturnEmptyWhenProcessedBookingEventDoesNotExist() {

    when(dynamoDbClient.getItem(any(GetItemRequest.class)))
        .thenReturn(GetItemResponse.builder().item(Map.of()).build());

    Optional<ProcessedBookingEvent> result =
        repository.loadByBookingIdAndEventType("booking-2", "BookingCreated");

    assertTrue(result.isEmpty());

    verify(dynamoDbClient).getItem(any(GetItemRequest.class));
  }

  @Test
  void shouldSaveProcessedBookingEvent() {

    ProcessedBookingEvent event =
        ProcessedBookingEvent.builder()
            .bookingId("booking-1")
            .eventType("BookingCreated")
            .processedAt(LocalDateTime.of(2026, 8, 13, 19, 0))
            .build();

    ProcessedBookingEvent result = repository.save(event);

    assertEquals(event, result);

    verify(dynamoDbClient).putItem(any(PutItemRequest.class));
  }
}
