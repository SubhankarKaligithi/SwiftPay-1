package com.swiftpay.ledger.repository;

import com.swiftpay.ledger.LedgerTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<LedgerTransaction,String> {


    List<LedgerTransaction> findBySenderId(String senderId);

    List<LedgerTransaction> findByReceiverId(String receiverId);
}
