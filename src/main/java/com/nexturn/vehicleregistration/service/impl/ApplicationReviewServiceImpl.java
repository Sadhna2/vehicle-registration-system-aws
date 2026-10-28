package com.nexturn.vehicleregistration.service.impl;

import static com.nexturn.vehicleregistration.service.WorkflowSupport.ensure;
import com.nexturn.vehicleregistration.dto.request.ApplicationReviewRequest;
import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;
import com.nexturn.vehicleregistration.entity.RTOEmployee;
import com.nexturn.vehicleregistration.entity.RegistrationCertificate;
import com.nexturn.vehicleregistration.entity.Vehicle;
import com.nexturn.vehicleregistration.entity.VehicleRegistrationApplication;
import com.nexturn.vehicleregistration.enums.ApplicationStatus;
import com.nexturn.vehicleregistration.enums.ApplicationType;
import com.nexturn.vehicleregistration.enums.FinalResult;
import com.nexturn.vehicleregistration.enums.InspectionStatus;
import com.nexturn.vehicleregistration.enums.PaymentStatus;
import com.nexturn.vehicleregistration.exception.ApplicationIdNotFoundException;
import com.nexturn.vehicleregistration.exception.EmployeeNotFoundException;
import com.nexturn.vehicleregistration.exception.InspectionNotPassedException;
import com.nexturn.vehicleregistration.exception.InvalidRequestException;
import com.nexturn.vehicleregistration.exception.PaymentRequiredException;
import com.nexturn.vehicleregistration.repository.ApplicationWorkflowRepository;
import com.nexturn.vehicleregistration.repository.OwnerPaymentRepository;
import com.nexturn.vehicleregistration.repository.RTOEmployeeRepository;
import com.nexturn.vehicleregistration.repository.RegistrationCertificateRepository;
import com.nexturn.vehicleregistration.service.ApplicationQueryService;
import com.nexturn.vehicleregistration.service.ApplicationReviewService;
import com.nexturn.vehicleregistration.service.AuditService;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Applies the original NEW registration state transitions and issues an RC only after payment and passed inspection. */
@Service
@Transactional
public class ApplicationReviewServiceImpl implements ApplicationReviewService {
    @Autowired
    private RTOEmployeeRepository employeeRepository;

    @Autowired
    private ApplicationWorkflowRepository applicationRepository;

    @Autowired
    private OwnerPaymentRepository paymentRepository;

    @Autowired
    private RegistrationCertificateRepository certificateRepository;

    @Autowired
    private ApplicationQueryService applicationQueryService;

    @Autowired
    private AuditService auditService;

    @Value("${vrs.registration.validity-years:15}")
    private int registrationYears;

    @Override
    public ApplicationDetailsResponse review(
            String referenceNumber,
            ApplicationReviewRequest request, Long employeeId) {
        VehicleRegistrationApplication application = applicationRepository
                .locked(referenceNumber)
                .orElseThrow(
                        () -> new ApplicationIdNotFoundException(referenceNumber));

        RTOEmployee employee = employeeRepository
                .findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        ensure(
                application.getApplicationType() == ApplicationType.NEW,
                "Only new vehicle registration is supported");

        ensure(
                application.getApplicationStatus() != ApplicationStatus.APPROVED
                        && application.getApplicationStatus()
                                != ApplicationStatus.REJECTED,
                "Application is closed");

        if (request.action() == null) {
            throw new InvalidRequestException("Review action is required");
        }

        String remarks = request.remarks();

        switch (request.action()) {
            case "START" -> startVerification(application);

            case "CORRECTION" ->
                    requestCorrection(application, employee, remarks);

            case "VERIFY" ->
                    verify(application, employee, remarks);

            case "SCHEDULE" ->
                    scheduleInspection(application, request.appointment());

            case "PASS", "FAIL" ->
                    recordInspection(
                            application, employee, request.action(), remarks);

            case "APPROVE" ->
                    approve(application, employee, remarks);

            case "REJECT" ->
                    reject(application, employee, remarks);

            default ->
                    throw new InvalidRequestException("Unknown review action");
        }

        application.setUpdatedDate(Instant.now());

        auditService.record(
                application, request.action(), remarks);

        return applicationQueryService.detail(application);
    }

    /** Moves a submitted application into verification. */
    private void startVerification(
            VehicleRegistrationApplication application) {
        ensure(
                application.getApplicationStatus() == ApplicationStatus.SUBMITTED,
                "Application must be submitted before verification");

        application.setApplicationStatus(ApplicationStatus.UNDER_VERIFICATION);
    }

    /** Records the correction request and the employee who requested it. */
    private void requestCorrection(
            VehicleRegistrationApplication application,
            RTOEmployee employee,
            String remarks) {
        ensure(
                application.getApplicationStatus()
                        == ApplicationStatus.UNDER_VERIFICATION,
                "Application must be under verification");

        ensure(
                remarks != null && !remarks.isBlank(),
                "Correction remarks are required");

        application.setApplicationStatus(ApplicationStatus.CORRECTION_REQUIRED);
        application.setVerificationRemark(remarks);
        application.setVerifiedBy(employee);
        application.setVerifiedAt(Instant.now());
    }

    /** Records verification and enables inspection scheduling. */
    private void verify(
            VehicleRegistrationApplication application,
            RTOEmployee employee,
            String remarks) {
        ensure(
                application.getApplicationStatus()
                        == ApplicationStatus.UNDER_VERIFICATION,
                "Start verification first");

        application.setApplicationStatus(ApplicationStatus.INSPECTION_PENDING);
        application.setVerificationRemark(remarks);
        application.setVerifiedBy(employee);
        application.setVerifiedAt(Instant.now());
    }

    /** Allows today or a future date only after verification. */
    private void scheduleInspection(
            VehicleRegistrationApplication application,
            LocalDate appointment) {
        ensure(
                application.getApplicationStatus()
                        == ApplicationStatus.INSPECTION_PENDING,
                "Verify the application before scheduling inspection");

        ensure(
                appointment != null && !appointment.isBefore(LocalDate.now()),
                "Choose today or a future inspection date");

        application.setInspectionScheduleDate(appointment);
        application.setInspectionStatus(InspectionStatus.PENDING);
        application.setInspectionRemark(null);
        application.setInspectedAt(null);
        application.setInspectedBy(null);
    }

    /** Records inspection results only after the scheduled appointment has occurred. */
    private void recordInspection(
            VehicleRegistrationApplication application,
            RTOEmployee employee,
            String action,
            String remarks) {
        ensure(
                application.getApplicationStatus()
                        == ApplicationStatus.INSPECTION_PENDING
                        && application.getInspectionScheduleDate() != null,
                "Schedule inspection first");

        ensure(
                !application.getInspectionScheduleDate()
                        .isAfter(LocalDate.now()),
                "Inspection appointment has not occurred yet");

        ensure(
                remarks != null && !remarks.isBlank(),
                "Inspection remarks are required");

        application.setInspectionStatus(
                "PASS".equals(action)
                        ? InspectionStatus.PASSED
                        : InspectionStatus.FAILED);

        application.setInspectionRemark(remarks);
        application.setInspectedBy(employee);
        application.setInspectedAt(Instant.now());
    }

    /** Requires a passed inspection and successful payment, then generates the original VR-plus-vehicle-ID RC. */
    private void approve(
            VehicleRegistrationApplication application,
            RTOEmployee employee,
            String remarks) {
        if (application.getApplicationStatus()
                        != ApplicationStatus.INSPECTION_PENDING
                || application.getInspectionStatus()
                        != InspectionStatus.PASSED) {
            throw new InspectionNotPassedException();
        }

        boolean paymentSuccessful = paymentRepository
                .findByApplicationApplicationRefNo(
                        application.getApplicationRefNo())
                .map(payment -> payment.getPaymentStatus() == PaymentStatus.SUCCESS)
                .orElse(false);

        if (!paymentSuccessful) {
            throw new PaymentRequiredException();
        }

        ensure(
                registrationYears > 0,
                "Registration validity must be greater than zero");

        Vehicle vehicle = application.getVehicle();

        ensure(
                vehicle.getTemporaryregisterNo() != null,
                "Vehicle temporary registration number is required");

        ensure(
                vehicle.getRegistrationcertificateNumber() == null
                        || vehicle.getRegistrationcertificateNumber().isBlank(),
                "Vehicle already has a registration certificate");

        LocalDate issueDate = LocalDate.now();
        LocalDate validTill = issueDate.plusYears(registrationYears);

        String registrationNumber = "VR" + vehicle.getTemporaryregisterNo();

        ensure(
                !certificateRepository.existsById(registrationNumber),
                "Registration certificate number already exists");

        vehicle.setRegistrationcertificateNumber(registrationNumber);
        vehicle.setFirstRegistrationDate(issueDate);
        vehicle.setRegistrationValidTill(validTill);

        RegistrationCertificate certificate = new RegistrationCertificate();
        certificate.setRegistrationNumber(registrationNumber);
        certificate.setApplication(application);
        certificate.setIssuedBy(employee);
        certificate.setIssuedDate(issueDate);
        certificate.setValidTill(validTill);
        certificate.setRegisteredOwner(vehicle.getCurrentOwner());

        certificateRepository.save(certificate);

        application.setApplicationStatus(ApplicationStatus.APPROVED);
        application.setFinalResult(FinalResult.APPROVED);
        application.setDecidedBy(employee);
        application.setDecidedAt(Instant.now());
        application.setDecisionRemarks(remarks);
    }

    /** Closes the application with mandatory rejection remarks. */
    private void reject(
            VehicleRegistrationApplication application,
            RTOEmployee employee,
            String remarks) {
        ensure(
                remarks != null && !remarks.isBlank(),
                "Rejection remarks are required");

        application.setApplicationStatus(ApplicationStatus.REJECTED);
        application.setFinalResult(FinalResult.REJECTED);
        application.setDecidedBy(employee);
        application.setDecidedAt(Instant.now());
        application.setDecisionRemarks(remarks);
    }
}
