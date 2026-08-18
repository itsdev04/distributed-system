package com.devworks.service;

import com.devworks.dto.PaymentRequest;
import io.github.resilience4j.retry.annotation.Retry;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class OrderService {
  private final WebClient webClient;

  public OrderService(WebClient webClient) {
    this.webClient = webClient;
  }

  @Retry(name = "paymentRetry", fallbackMethod = "paymentFallback")
  public String createOrder(String orderId, BigDecimal amount) {
    String idempotencyKey = "PAY-KEY-" + orderId;
    log.info("Order created");
    String response =
        webClient
            .post()
            .uri("http://localhost:8082/payments")
            .header("X-Idempotency-Key", idempotencyKey)
            .bodyValue(new PaymentRequest(orderId, amount))
            .retrieve()
            .bodyToMono(String.class)
            // How this retry works ? How can I configure the same using resilence4J
            // .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(1)))
            .block();
    return response;
  }

  public String paymentFallback(Throwable t) {
    log.error("Fallback triggered due to error: {}", t.getMessage());
    return "Order creation failed: Payment service is unavailable.";
  }
}
