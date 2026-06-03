package com.swiftpay.gatewayservice.service;

import com.swiftpay.common.event.PaymentInitiatedEvent;

import com.swiftpay.gatewayservice.entity.PaymentEntity;
import com.swiftpay.gatewayservice.repository.PaymentRepository;
import com.swiftpay.gatewayservice.PaymentRequest;
import com.swiftpay.gatewayservice.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, PaymentInitiatedEvent> kafkaTemplate;
    private final StringRedisTemplate redisTemplate;

//////
    public PaymentEntity create(PaymentRequest request) {

        validate(request);

        String redisKey = "idem:" + request.getTransactionId();

        Boolean isNew = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "PROCESSING", Duration.ofHours(24));

        if (Boolean.FALSE.equals(isNew)) {
            throw new IllegalArgumentException("Duplicate transaction request");
        }

        PaymentEntity payment = PaymentEntity.builder()
                .transactionId(request.getTransactionId())
                .senderId(request.getSenderId())
                .receiverId(request.getReceiverId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        paymentRepository.save(payment);

        PaymentInitiatedEvent event = new PaymentInitiatedEvent(
                payment.getTransactionId(),
                payment.getSenderId(),
                payment.getReceiverId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getCreatedAt()
        );

        kafkaTemplate.send("payment.initiated", payment.getTransactionId(), event);

        return payment;
    }
    private void validate(PaymentRequest request) {
        if (request.getTransactionId() == null || request.getTransactionId().isBlank()) {
            throw new IllegalArgumentException("transactionId is required");
        }

        if (request.getSenderId() == null || request.getSenderId().isBlank()) {
            throw new IllegalArgumentException("senderId is required");
        }

        if (request.getReceiverId() == null || request.getReceiverId().isBlank()) {
            throw new IllegalArgumentException("receiverId is required");
        }

        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
    }


}
