package com.swiftpay.gatewayservice.controller;


import com.swiftpay.gatewayservice.entity.PaymentEntity;
import com.swiftpay.gatewayservice.PaymentRequest;
import com.swiftpay.gatewayservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentEntity> createPayment(@RequestBody PaymentRequest request) {
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(paymentService.create(request));
    }
}
//