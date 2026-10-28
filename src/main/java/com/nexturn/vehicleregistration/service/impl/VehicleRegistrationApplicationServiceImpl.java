package com.nexturn.vehicleregistration.service.impl;

import com.nexturn.vehicleregistration.dto.request.ApplicationReviewRequest;
import com.nexturn.vehicleregistration.dto.request.NewVehicleRegistrationRequest;
import com.nexturn.vehicleregistration.dto.request.PaymentRequest;
import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;
import com.nexturn.vehicleregistration.dto.response.ApplicationSummaryResponse;
import com.nexturn.vehicleregistration.dto.response.RegistrationCertificateResponse;
import com.nexturn.vehicleregistration.dto.response.VehicleResponse;
import com.nexturn.vehicleregistration.service.ApplicationQueryService;
import com.nexturn.vehicleregistration.service.ApplicationReviewService;
import com.nexturn.vehicleregistration.service.PaymentService;
import com.nexturn.vehicleregistration.service.RegistrationSubmissionService;
import com.nexturn.vehicleregistration.service.VehicleRegistrationApplicationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Delegates registration submission, payment, review and query operations to their dedicated services. */
@Service
public class VehicleRegistrationApplicationServiceImpl
        implements VehicleRegistrationApplicationService {
    @Autowired
    private RegistrationSubmissionService registrationSubmissionService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ApplicationReviewService applicationReviewService;

    @Autowired
    private ApplicationQueryService applicationQueryService;

    @Override
    public ApplicationDetailsResponse submit(
            NewVehicleRegistrationRequest request, Long ownerId) {
        return registrationSubmissionService.submit(request, ownerId);
    }

    @Override
    public ApplicationDetailsResponse correct(
            String referenceNumber,
            NewVehicleRegistrationRequest request) {
        return registrationSubmissionService.correct(
                referenceNumber, request);
    }

    @Override
    public ApplicationDetailsResponse pay(
            String referenceNumber,
            PaymentRequest request) {
        return paymentService.pay(referenceNumber, request);
    }

    @Override
    public ApplicationDetailsResponse review(
            String referenceNumber,
            ApplicationReviewRequest request, Long employeeId) {
        return applicationReviewService.review(
                referenceNumber, request, employeeId);
    }

    @Override
    public List<ApplicationSummaryResponse> list(Long ownerId) {
        return applicationQueryService.list(ownerId);
    }

    @Override
    public ApplicationDetailsResponse get(
            String referenceNumber) {
        return applicationQueryService.get(referenceNumber);
    }

    @Override
    public List<VehicleResponse> vehicles(Long ownerId) {
        return applicationQueryService.vehicles(ownerId);
    }

    @Override
    public RegistrationCertificateResponse certificate(
            String referenceNumber) {
        return applicationQueryService.certificate(referenceNumber);
    }
}
