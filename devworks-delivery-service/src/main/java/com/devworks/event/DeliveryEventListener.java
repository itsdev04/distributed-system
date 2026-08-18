package com.devworks.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class DeliveryEventListener {
    @KafkaListener(topics = "payment-events", groupId = "delivery-service-group")
    public void handlePaymentCompleted(PaymentCompletedEvent event){
        log.info("Received PaymentCompletedEvent for Order ID: {}", event.orderId());
        processDelivery(event);
    }

    private void processDelivery(PaymentCompletedEvent event) {
        log.info("Initiating delivery scheduling for payment transaction: {}", event.paymentId());
    }
}
