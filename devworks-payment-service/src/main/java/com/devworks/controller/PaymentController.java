package com.devworks.controller;

import com.devworks.dto.PaymentRequest;
import com.devworks.dto.PaymentResponse;
import com.devworks.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired private PaymentService paymentService;
    @PostMapping
    public ResponseEntity<PaymentResponse> handlePayment(
            @RequestHeader(value = "X-Idempotency-Key", required = true) String idempotencyKey,
            @RequestBody PaymentRequest request) {

        PaymentResponse response = paymentService.processPayment(idempotencyKey, request);
        return ResponseEntity.ok(response);
    }
}
