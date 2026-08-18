package com.devworks.controller;

import com.devworks.dto.PaymentRequest;
import com.devworks.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/orders")
public class OrderController {
  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping
  public ResponseEntity<String> createOrder(
      @RequestHeader(value = "X-Idempotency-Key", required = true) String idempotencyKey,
      @RequestBody PaymentRequest request) {
    log.info("Received request to create order");
    String response = orderService.createOrder(request.getOrderId(), request.getAmount());
    return new ResponseEntity<>(response, HttpStatus.OK);
  }
}
