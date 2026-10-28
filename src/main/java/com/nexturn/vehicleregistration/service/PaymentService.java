package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.dto.request.PaymentRequest;
import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;

/** Defines the original simulated registration payment operation. */
public interface PaymentService {
    ApplicationDetailsResponse pay(
            String referenceNumber,
            PaymentRequest request);
}
