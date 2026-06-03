package com.swiftpay.ledger.controller;

import com.swiftpay.ledger.LedgerTransaction;
import com.swiftpay.ledger.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/ledger")
@RequiredArgsConstructor
public class TransactionController {


    private final TransactionRepository repository;

    @GetMapping("/{userId}")
    public List<LedgerTransaction> history(
            @PathVariable String userId
    ) {

        return repository.findBySenderId(userId);
    }
}
