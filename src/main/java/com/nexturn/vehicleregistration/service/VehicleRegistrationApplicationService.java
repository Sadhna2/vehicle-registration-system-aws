package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.dto.request.ApplicationReviewRequest;
import com.nexturn.vehicleregistration.dto.request.NewVehicleRegistrationRequest;
import com.nexturn.vehicleregistration.dto.request.PaymentRequest;
import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;
import com.nexturn.vehicleregistration.dto.response.ApplicationSummaryResponse;
import com.nexturn.vehicleregistration.dto.response.RegistrationCertificateResponse;
import com.nexturn.vehicleregistration.dto.response.VehicleResponse;

import java.util.List;

/** Defines submission, payment, RTO review, tracking and certificate operations for new vehicles. */
public interface VehicleRegistrationApplicationService {
    ApplicationDetailsResponse submit(
            NewVehicleRegistrationRequest request, Long ownerId);

    ApplicationDetailsResponse correct(
            String referenceNumber,
            NewVehicleRegistrationRequest request);

    ApplicationDetailsResponse pay(
            String referenceNumber,
            PaymentRequest request);

    ApplicationDetailsResponse review(
            String referenceNumber,
            ApplicationReviewRequest request, Long employeeId);

    List<ApplicationSummaryResponse> list(Long ownerId);

    ApplicationDetailsResponse get(
            String referenceNumber);

    List<VehicleResponse> vehicles(Long ownerId);

    RegistrationCertificateResponse certificate(
            String referenceNumber);
}
