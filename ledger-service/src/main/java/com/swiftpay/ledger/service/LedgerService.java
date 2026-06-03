package com.swiftpay.ledger.service;
import com.swiftpay.common.event.PaymentCompletedEvent;
import com.swiftpay.common.event.PaymentInitiatedEvent;
import com.swiftpay.ledger.*;
import com.swiftpay.ledger.entity.Account;
import com.swiftpay.ledger.repository.AccountRepository;
import com.swiftpay.ledger.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class LedgerService {
    private final AccountRepository accountRepository;

    private final TransactionRepository transactionRepository;

    private final KafkaTemplate<String,Object> kafkaTemplate;

    @Transactional
    public void process(PaymentInitiatedEvent event) {

        Account sender =
                accountRepository.findByUserIdForUpdate(event.getSenderId())
                        .orElseThrow();

        Account receiver =
                accountRepository.findByUserIdForUpdate(event.getReceiverId())
                        .orElseThrow();

        if(sender.getBalance().compareTo(event.getAmount()) < 0) {

            kafkaTemplate.send(
                    "payment.failed",
                    new PaymentFailedEvent(
                            event.getTransactionId(),
                            "Insufficient funds"
                    )
            );

            throw new InsufficientFundsException(
                    "Insufficient balance"
            );
        }

        sender.setBalance(
                sender.getBalance()
                        .subtract(event.getAmount())
        );

        receiver.setBalance(
                receiver.getBalance()
                        .add(event.getAmount())
        );

        accountRepository.save(sender);
        accountRepository.save(receiver);

        LedgerTransaction tx =
                new LedgerTransaction();

        tx.setTransactionId(event.getTransactionId());
        tx.setSenderId(event.getSenderId());
        tx.setReceiverId(event.getReceiverId());
        tx.setAmount(event.getAmount());
        tx.setCurrency(event.getCurrency());
        tx.setStatus("COMPLETED");
        tx.setCreatedAt(Instant.now());

        transactionRepository.save(tx);

        kafkaTemplate.send(
                "payment.completed",
                new PaymentCompletedEvent(
                        event.getTransactionId(),
                        event.getSenderId(),
                        event.getReceiverId(),
                        event.getAmount(),
                        Instant.now()
                )
        );
    }
}
