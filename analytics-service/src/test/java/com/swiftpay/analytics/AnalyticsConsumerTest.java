package com.swiftpay.analytics;

import com.swiftpay.common.event.PaymentCompletedEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatCode;

class AnalyticsConsumerTest {

    @Test
    void consumesCompletedEventWithoutError() {
        AnalyticsConsumer consumer = new AnalyticsConsumer();

        PaymentCompletedEvent event = new PaymentCompletedEvent(
                "txn-1", "sender", "receiver", new BigDecimal("100"), Instant.now());

        assertThatCode(() -> consumer.consume(event)).doesNotThrowAnyException();
    }
}
