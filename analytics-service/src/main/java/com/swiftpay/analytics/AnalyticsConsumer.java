package com.swiftpay.analytics;
import com.swiftpay.common.event.PaymentCompletedEvent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AnalyticsConsumer {

    @KafkaListener(
            topics = "payment.completed",
            groupId = "analytics-group"
    )
    public void consume(PaymentCompletedEvent event) {
        log.info(
                "Analytics received completed payment: tx={}, amount={}",
                event.getTransactionId(),
                event.getAmount()
        );
    }
}