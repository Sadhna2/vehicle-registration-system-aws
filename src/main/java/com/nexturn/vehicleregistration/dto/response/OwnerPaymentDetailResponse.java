package com.nexturn.vehicleregistration.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

import com.nexturn.vehicleregistration.enums.PaymentMethod;
import com.nexturn.vehicleregistration.enums.PaymentStatus;

/** Returns the stored payment simulation result without full card details. */
public record OwnerPaymentDetailResponse(
        Long paymentId,
        String applicationRefNo,
        BigDecimal amountPaid,
        String cardLastFourDigit,
        String transactionReferenceId,
        PaymentStatus paymentStatus,
        Instant paidAt,
        PaymentMethod paymentMethod
) {
}
