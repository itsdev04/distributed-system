package com.devworks.service;

import com.devworks.dto.PaymentRequest;
import com.devworks.dto.PaymentResponse;
import com.devworks.event.PaymentCompletedEvent;
import com.devworks.publisher.PaymentEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
public class PaymentService {

    private final PaymentEventPublisher eventPublisher;

    public PaymentService(PaymentEventPublisher eventPublisher){
        this.eventPublisher = eventPublisher;
    }
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    public PaymentResponse processPayment(String idempotencyKey, PaymentRequest request){
        String cacheKey = "idempotency:payment:"+idempotencyKey;
        String lockKey = "lock:payment:"+idempotencyKey;

        PaymentResponse cachedResponse = (PaymentResponse) redisTemplate.opsForValue().get(cacheKey);
        if(cachedResponse!= null){
            log.info("Idempotent request hit for key: {}. Returning saved response.",idempotencyKey);
            return cachedResponse;
        }

        Boolean acquiredLock = redisTemplate.opsForValue()
                .setIfAbsent(lockKey,"PROCESSING", Duration.ofSeconds(30));

        if(Boolean.FALSE.equals(acquiredLock)){
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Concurrent payment processing in progress.");
        }
        try{
            PaymentResponse response = executePaymentCall(request);
            redisTemplate.opsForValue()
                    .set(cacheKey, response,Duration.ofMinutes(1));
        return response;
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    private PaymentResponse executePaymentCall(PaymentRequest request) {

        PaymentResponse response = new PaymentResponse("TXN_" + UUID.randomUUID(), request.getOrderId(), "SUCCESS");
        PaymentCompletedEvent event = new PaymentCompletedEvent(
          response.getOrderId(),
          response.getTransactionId(),
          request.getAmount(),
                response.getStatus()
        );
        eventPublisher.publishPaymentCompleted(event);
        return response;
    }
}
