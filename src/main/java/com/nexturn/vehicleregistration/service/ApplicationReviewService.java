package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.dto.request.ApplicationReviewRequest;
import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;

/** Defines verification, inspection, decisions and certificate issuance for NEW applications. */
public interface ApplicationReviewService {
    ApplicationDetailsResponse review(
            String referenceNumber,
            ApplicationReviewRequest request, Long employeeId);
}
