package com.nexturn.vehicleregistration.service.impl;

import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;
import com.nexturn.vehicleregistration.dto.response.ApplicationSummaryResponse;
import com.nexturn.vehicleregistration.dto.response.OwnerPaymentDetailResponse;
import com.nexturn.vehicleregistration.dto.response.OwnerResponse;
import com.nexturn.vehicleregistration.dto.response.RegistrationCertificateResponse;
import com.nexturn.vehicleregistration.dto.response.VehicleResponse;
import com.nexturn.vehicleregistration.entity.Owner;
import com.nexturn.vehicleregistration.entity.OwnerPaymentDetail;
import com.nexturn.vehicleregistration.entity.RegistrationCertificate;
import com.nexturn.vehicleregistration.entity.Vehicle;
import com.nexturn.vehicleregistration.entity.VehicleRegistrationApplication;
import com.nexturn.vehicleregistration.exception.ReferenceNumberNotFoundException;
import com.nexturn.vehicleregistration.exception.RegistrationCertificateNotFoundException;
import com.nexturn.vehicleregistration.repository.ApplicationWorkflowRepository;
import com.nexturn.vehicleregistration.repository.OwnerPaymentRepository;
import com.nexturn.vehicleregistration.repository.RegistrationCertificateRepository;
import com.nexturn.vehicleregistration.repository.VechileRepository;
import com.nexturn.vehicleregistration.service.ApplicationQueryService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reads the full existing application response with its persisted payment and issued certificate, without a mapper layer. */
@Service
@Transactional(readOnly = true)
public class ApplicationQueryServiceImpl implements ApplicationQueryService {
    @Autowired
    private VechileRepository vehicleRepository;

    @Autowired
    private ApplicationWorkflowRepository applicationRepository;

    @Autowired
    private OwnerPaymentRepository paymentRepository;

    @Autowired
    private RegistrationCertificateRepository certificateRepository;

    @Override
    public ApplicationDetailsResponse detail(
            VehicleRegistrationApplication application) {
        String referenceNumber = application.getApplicationRefNo();

        return toApplicationDetailsResponse(
                application,
                paymentRepository
                        .findByApplicationApplicationRefNo(referenceNumber)
                        .orElse(null),
                certificateRepository
                        .findByApplicationApplicationRefNo(referenceNumber)
                        .orElse(null));
    }

    @Override
    public List<ApplicationSummaryResponse> list(Long ownerId) {
        Sort sort = Sort.by("submittedDate").descending();
        List<VehicleRegistrationApplication> applications = ownerId == null
                ? applicationRepository.findAll(sort)
                : applicationRepository.findByApplicantOwnerId(ownerId, sort);
        return applications.stream()
                .map(this::toApplicationSummaryResponse)
                .toList();
    }

    @Override
    public ApplicationDetailsResponse get(
            String referenceNumber) {
        VehicleRegistrationApplication application =
                applicationRepository.findById(referenceNumber)
                        .orElseThrow(() -> new ReferenceNumberNotFoundException(referenceNumber));

        return detail(application);
    }

    @Override
    public List<VehicleResponse> vehicles(Long ownerId) {
        return (ownerId == null ? vehicleRepository.findAll()
                : vehicleRepository.findByCurrentOwnerOwnerId(ownerId))
                .stream()
                .map(this::toVehicleResponse)
                .toList();
    }

    @Override
    public RegistrationCertificateResponse certificate(
            String referenceNumber) {
        VehicleRegistrationApplication application =
                applicationRepository.findById(referenceNumber)
                        .orElseThrow(() -> new ReferenceNumberNotFoundException(referenceNumber));

        String registrationNumber = application.getVehicle()
                .getRegistrationcertificateNumber();

        if (registrationNumber == null || registrationNumber.isBlank()) {
            throw new RegistrationCertificateNotFoundException(registrationNumber);
        }

        RegistrationCertificate certificate = certificateRepository
                .findById(registrationNumber)
                .orElseThrow(
                        () -> new RegistrationCertificateNotFoundException(
                                registrationNumber));

        return toCertificateResponse(certificate);
    }

    private ApplicationDetailsResponse toApplicationDetailsResponse(
            VehicleRegistrationApplication application,
            OwnerPaymentDetail payment,
            RegistrationCertificate certificate) {
        if (application == null) {
            return null;
        }

        Long vehicleId = null;
        Long applicantId = null;
        Long feeRuleId = null;
        Long verifiedByEmployeeId = null;
        Long inspectedByEmployeeId = null;
        Long decidedByEmployeeId = null;

        if (application.getVehicle() != null) {
            vehicleId = application.getVehicle().getTemporaryregisterNo();
        }

        if (application.getApplicant() != null) {
            applicantId = application.getApplicant().getOwnerId();
        }

        if (application.getFeeRule() != null) {
            feeRuleId = application.getFeeRule().getFeeRuleId();
        }

        if (application.getVerifiedBy() != null) {
            verifiedByEmployeeId = application.getVerifiedBy().getEmployeeId();
        }

        if (application.getInspectedBy() != null) {
            inspectedByEmployeeId = application.getInspectedBy().getEmployeeId();
        }

        if (application.getDecidedBy() != null) {
            decidedByEmployeeId = application.getDecidedBy().getEmployeeId();
        }

        return new ApplicationDetailsResponse(
                application.getApplicationRefNo(),
                vehicleId,
                applicantId,
                application.getApplicationType(),
                application.getApplicationStatus(),
                feeRuleId,
                application.getPayableAmount(),
                application.getPreviousValidUntil(),
                application.getSubmittedDate(),
                application.getUpdatedDate(),
                application.getVerificationRemark(),
                verifiedByEmployeeId,
                application.getVerifiedAt(),
                application.getInspectionScheduleDate(),
                application.getInspectionRemark(),
                inspectedByEmployeeId,
                application.getInspectedAt(),
                decidedByEmployeeId,
                application.getDecidedAt(),
                application.getDecisionRemarks(),
                application.getInspectionStatus(),
                application.getFinalResult(),
                application.getVersion(),
                toVehicleResponse(application.getVehicle()),
                toOwnerResponse(application.getApplicant()),
                toPaymentResponse(payment),
                toCertificateResponse(certificate));
    }

    private ApplicationSummaryResponse toApplicationSummaryResponse(VehicleRegistrationApplication a) {
        return new ApplicationSummaryResponse(
                a.getApplicationRefNo(),
                a.getApplicationType(),
                a.getApplicationStatus(),
                a.getPayableAmount(),
                a.getSubmittedDate(),
                a.getUpdatedDate(),
                a.getVehicle().getTemporaryregisterNo(),
                a.getVehicle().getManufacturerName() + " " + a.getVehicle().getModelName(),
                a.getApplicant().getFirstName() + " " + a.getApplicant().getLastName());
    }

    private OwnerResponse toOwnerResponse(Owner owner) {
                if (owner == null) {
                        return null;
                }

                return new OwnerResponse(
                                owner.getOwnerId(),
                                owner.getFirstName(),
                                owner.getLastName(),
                                owner.getEmailAddress(),
                                owner.getPhoneNumber(),
                                owner.getDateOfBirth(),
                                owner.getIdentityProofType(),
                                owner.getIdentityProofNumber(),
                                owner.getAddress(),
                                owner.getCityName(),
                                owner.getStateName(),
                                owner.getPincode(),
                                owner.getStatus(),
                                owner.getDateOfCreation(),
                                owner.getDateOfUpdate()
                );
        }

    private VehicleResponse toVehicleResponse(Vehicle vehicle) {
                if (vehicle == null) {
                        return null;
                }

                return new VehicleResponse(
                                vehicle.getTemporaryregisterNo(),
                                toOwnerResponse(vehicle.getCurrentOwner()),
                                vehicle.getVehicleCategory(),
                                vehicle.getManufacturerName(),
                                vehicle.getModelName(),
                                vehicle.getChassisNumber(),
                                vehicle.getEngineNumber(),
                                vehicle.getFuelType(),
                                vehicle.getManufactureYear(),
                                vehicle.getRegistrationcertificateNumber(),
                                vehicle.getFirstRegistrationDate(),
                                vehicle.getRegistrationValidTill(),
                                vehicle.getColorVariant()
                );
        }

    private OwnerPaymentDetailResponse toPaymentResponse(
                        OwnerPaymentDetail payment) {
                if (payment == null) {
                        return null;
                }

                String applicationRefNo = null;

                if (payment.getApplication() != null) {
                        applicationRefNo =
                                        payment.getApplication().getApplicationRefNo();
                }

                return new OwnerPaymentDetailResponse(
                                payment.getPaymentId(),
                                applicationRefNo,
                                payment.getAmountPaid(),
                                payment.getCardLastFourDigit(),
                                payment.getTransactionReferenceId(),
                                payment.getPaymentStatus(),
                                payment.getPaidAt(),
                                payment.getPaymentMethod()
                );
        }

    private RegistrationCertificateResponse toCertificateResponse(
                        RegistrationCertificate certificate) {
                if (certificate == null) {
                        return null;
                }

                String applicationRefNo = null;
                Long employeeId = null;
                String employeeName = null;
                Long ownerId = null;
                String ownerName = null;

                if (certificate.getApplication() != null) {
                        applicationRefNo =
                                        certificate.getApplication().getApplicationRefNo();
                }

                if (certificate.getIssuedBy() != null) {
                        employeeId = certificate.getIssuedBy().getEmployeeId();

                        employeeName =
                                        certificate.getIssuedBy().getFirstName()
                                        + " "
                                        + certificate.getIssuedBy().getLastName();
                }

                if (certificate.getRegisteredOwner() != null) {
                        ownerId = certificate.getRegisteredOwner().getOwnerId();

                        ownerName =
                                        certificate.getRegisteredOwner().getFirstName()
                                        + " "
                                        + certificate.getRegisteredOwner().getLastName();
                }

                return new RegistrationCertificateResponse(
                                certificate.getRegistrationNumber(),
                                applicationRefNo,
                                employeeId,
                                employeeName,
                                certificate.getIssuedDate(),
                                certificate.getValidTill(),
                                ownerId,
                                ownerName
                );
        }
}
