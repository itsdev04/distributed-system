package com.devworks.event;

import java.math.BigDecimal;

public record PaymentCompletedEvent(
        String paymentId,
        String orderId,
        BigDecimal amount,
        String status
) {}