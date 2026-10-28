package com.nexturn.vehicleregistration.dto.response;

import com.nexturn.vehicleregistration.enums.ApplicationStatus;
import com.nexturn.vehicleregistration.enums.ApplicationType;
import com.nexturn.vehicleregistration.enums.FinalResult;
import com.nexturn.vehicleregistration.enums.InspectionStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Returns the existing application response contract with persisted payment and certificate details when available. */
public record ApplicationDetailsResponse(
    String applicationRefNo,
    Long vehicleId,
    Long applicantId,
    ApplicationType applicationType,
    ApplicationStatus applicationStatus,
    Long feeRuleId,
    BigDecimal payableAmount,
    LocalDate previousValidUntil,
    Instant submittedDate,
    Instant updatedDate,
    String verificationRemark,
    Long verifiedByEmployeeId,
    Instant verifiedAt,
    LocalDate inspectionScheduleDate,
    String inspectionRemark,
    Long inspectedByEmployeeId,
    Instant inspectedAt,
    Long decidedByEmployeeId,
    Instant decidedAt,
    String decisionRemarks,
    InspectionStatus inspectionStatus,
    FinalResult finalResult,
    long version,
    VehicleResponse vehicle,
    OwnerResponse applicant,
    OwnerPaymentDetailResponse payment,
    RegistrationCertificateResponse certificate) {}
