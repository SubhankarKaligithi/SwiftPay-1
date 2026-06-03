package com.swiftpay.ledger;

import com.swiftpay.common.event.PaymentCompletedEvent;
import com.swiftpay.common.event.PaymentInitiatedEvent;
import com.swiftpay.ledger.entity.Account;
import com.swiftpay.ledger.repository.AccountRepository;
import com.swiftpay.ledger.repository.TransactionRepository;
import com.swiftpay.ledger.service.LedgerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private LedgerService ledgerService;

    private PaymentInitiatedEvent event(BigDecimal amount) {
        return new PaymentInitiatedEvent(
                "txn-1", "sender", "receiver", amount, "INR", Instant.now());
    }

    private Account account(String userId, String balance) {
        Account account = new Account();
        account.setUserId(userId);
        account.setBalance(new BigDecimal(balance));
        return account;
    }

    @Test
    void transfersFundsAndPublishesCompletedEvent() {
        Account sender = account("sender", "100");
        Account receiver = account("receiver", "10");
        when(accountRepository.findByUserIdForUpdate("sender")).thenReturn(Optional.of(sender));
        when(accountRepository.findByUserIdForUpdate("receiver")).thenReturn(Optional.of(receiver));

        ledgerService.process(event(new BigDecimal("30")));

        assertThat(sender.getBalance()).isEqualByComparingTo("70");
        assertThat(receiver.getBalance()).isEqualByComparingTo("40");

        ArgumentCaptor<LedgerTransaction> txCaptor = ArgumentCaptor.forClass(LedgerTransaction.class);
        verify(transactionRepository).save(txCaptor.capture());
        assertThat(txCaptor.getValue().getStatus()).isEqualTo("COMPLETED");
        assertThat(txCaptor.getValue().getTransactionId()).isEqualTo("txn-1");

        verify(kafkaTemplate).send(eq("payment.completed"), any(PaymentCompletedEvent.class));
        verify(kafkaTemplate, never()).send(eq("payment.failed"), any());
    }

    @Test
    void rejectsPaymentWhenSenderHasInsufficientFunds() {
        Account sender = account("sender", "10");
        Account receiver = account("receiver", "0");
        when(accountRepository.findByUserIdForUpdate("sender")).thenReturn(Optional.of(sender));
        when(accountRepository.findByUserIdForUpdate("receiver")).thenReturn(Optional.of(receiver));

        assertThatThrownBy(() -> ledgerService.process(event(new BigDecimal("50"))))
                .isInstanceOf(InsufficientFundsException.class);

        verify(kafkaTemplate).send(eq("payment.failed"), any(PaymentFailedEvent.class));
        verify(transactionRepository, never()).save(any());
        assertThat(sender.getBalance()).isEqualByComparingTo("10");
        assertThat(receiver.getBalance()).isEqualByComparingTo("0");
    }
}
