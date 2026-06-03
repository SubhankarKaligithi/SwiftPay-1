package com.swiftpay.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent{
        private String transactionId;
        private  String senderId;
        private  String receiverId;
        private  BigDecimal amount;
        private Instant completedAt;

}
