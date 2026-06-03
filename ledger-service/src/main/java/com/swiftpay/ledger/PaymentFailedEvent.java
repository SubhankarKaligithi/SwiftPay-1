package com.swiftpay.ledger;

public record PaymentFailedEvent(String transactionId,
                                 String reason) {
}
