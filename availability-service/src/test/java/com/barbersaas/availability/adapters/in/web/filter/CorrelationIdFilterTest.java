package com.barbersaas.availability.adapters.in.web.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.barbersaas.shared.logging.CorrelationIdHolder;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

  private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

  private final CorrelationIdFilter filter = new CorrelationIdFilter();

  @AfterEach
  void cleanup() {
    CorrelationIdHolder.clear();
    MDC.clear();
  }

  @Test
  void shouldUseCorrelationIdFromRequestHeader() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setMethod("GET");
    request.setRequestURI("/api/v1/availability/test");
    request.addHeader(CORRELATION_ID_HEADER, "corr-1121");

    MockHttpServletResponse response = new MockHttpServletResponse();

    AtomicReference<String> correlationIdInsideChain = new AtomicReference<>();
    AtomicReference<String> mdcCorrelationIdInsideChain = new AtomicReference<>();

    filter.doFilter(
        request,
        response,
        (servletRequest, servletResponse) -> {
          correlationIdInsideChain.set(CorrelationIdHolder.get());
          mdcCorrelationIdInsideChain.set(MDC.get("correlationId"));
        });

    assertEquals("corr-1121", response.getHeader(CORRELATION_ID_HEADER));
    assertEquals("corr-1121", correlationIdInsideChain.get());
    assertEquals("corr-1121", mdcCorrelationIdInsideChain.get());

    assertNull(CorrelationIdHolder.get());
    assertNull(MDC.get("correlationId"));
  }

  @Test
  void shouldGenerateCorrelationIdWhenHeaderIsMissing() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setMethod("GET");
    request.setRequestURI("/actuator/health");

    MockHttpServletResponse response = new MockHttpServletResponse();

    AtomicReference<String> correlationIdInsideChain = new AtomicReference<>();

    filter.doFilter(
        request,
        response,
        (servletRequest, servletResponse) ->
            correlationIdInsideChain.set(CorrelationIdHolder.get()));

    String generatedCorrelationId = response.getHeader(CORRELATION_ID_HEADER);

    assertNotNull(generatedCorrelationId);
    assertFalse(generatedCorrelationId.isBlank());

    assertEquals(generatedCorrelationId, correlationIdInsideChain.get());

    assertNull(CorrelationIdHolder.get());
    assertNull(MDC.get("correlationId"));
  }

  @Test
  void shouldGenerateCorrelationIdWhenHeaderIsBlank() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setMethod("GET");
    request.setRequestURI("/actuator/health");
    request.addHeader(CORRELATION_ID_HEADER, " ");

    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, (servletRequest, servletResponse) -> {});

    String generatedCorrelationId = response.getHeader(CORRELATION_ID_HEADER);

    assertNotNull(generatedCorrelationId);
    assertFalse(generatedCorrelationId.isBlank());

    assertNull(CorrelationIdHolder.get());
    assertNull(MDC.get("correlationId"));
  }
}
