package com.swiftpay.ledger;

import com.swiftpay.common.event.PaymentInitiatedEvent;
import com.swiftpay.ledger.service.LedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentConsumer {

    private final LedgerService ledgerService;

    @RetryableTopic(
            attempts = "5"
    )
    @KafkaListener(
            topics = "payment.initiated",
            groupId = "ledger-group"
    )
    public void consume(
            PaymentInitiatedEvent event
    ) {

        log.info("Processing {}",event.getTransactionId());

        ledgerService.process(event);
    }
}