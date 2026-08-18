package com.devworks.publisher;

import com.devworks.event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventPublisher {
private final KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;
private static final String TOPIC = "payment-events";

public void publishPaymentCompleted(PaymentCompletedEvent event){
    log.info("Publishing PaymentCompletedEvent for Order ID: {}", event.orderId());
    kafkaTemplate.send(TOPIC,event.orderId(), event)
            .whenComplete((result,ex) -> {
                if(ex==null){
                    log.info("Successfully published event to partition {}", result.getRecordMetadata().partition());
                } else {
                    log.error("Failed to published payment event", ex);
                }
            });

}
}
