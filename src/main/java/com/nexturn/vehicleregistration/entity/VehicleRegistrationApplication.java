package com.nexturn.vehicleregistration.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.nexturn.vehicleregistration.enums.ApplicationStatus;
import com.nexturn.vehicleregistration.enums.ApplicationType;
import com.nexturn.vehicleregistration.enums.FinalResult;
import com.nexturn.vehicleregistration.enums.InspectionStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/** Maps new-vehicle applications and their existing workflow fields. */
@Entity
@Table(name = "vehicle_registration_applications")
public class VehicleRegistrationApplication {
  @Id
  @Column(length = 30)
  private String applicationRefNo;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "vehicle_id")
  private Vehicle vehicle;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "applicant_id")
  private Owner applicant;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ApplicationType applicationType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ApplicationStatus applicationStatus = ApplicationStatus.SUBMITTED;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "fee_rule_id")
  private RegistrationFeeRule feeRule;

  @Column(precision = 10, scale = 2, nullable = false)
  private BigDecimal payableAmount;

  private LocalDate previousValidUntil;

  @Column(nullable = false)
  private Instant submittedDate = Instant.now();

  @Column(nullable = false)
  private Instant updatedDate = Instant.now();

  @Column(length = 500)
  private String verificationRemark;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "verified_by")
  private RTOEmployee verifiedBy;

  private Instant verifiedAt;
  private LocalDate inspectionScheduleDate;

  @Column(length = 500)
  private String inspectionRemark;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "inspected_by")
  private RTOEmployee inspectedBy;

  private Instant inspectedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "decided_by")
  private RTOEmployee decidedBy;

  private Instant decidedAt;

  @Column(length = 500)
  private String decisionRemarks;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private InspectionStatus inspectionStatus = InspectionStatus.PENDING;

  @Enumerated(EnumType.STRING)
  private FinalResult finalResult;

  public FinalResult getFinalResult() {
    return finalResult;
  }

  public void setFinalResult(FinalResult value) {
    finalResult = value;
  }

  @Version private long version;

  public String getApplicationRefNo() {
    return applicationRefNo;
  }

  public void setApplicationRefNo(String value) {
    this.applicationRefNo = value;
  }

  public Vehicle getVehicle() {
    return vehicle;
  }

  public void setVehicle(Vehicle value) {
    this.vehicle = value;
  }

  public Owner getApplicant() {
    return applicant;
  }

  public void setApplicant(Owner value) {
    this.applicant = value;
  }

  public ApplicationType getApplicationType() {
    return applicationType;
  }

  public void setApplicationType(ApplicationType value) {
    this.applicationType = value;
  }

  public ApplicationStatus getApplicationStatus() {
    return applicationStatus;
  }

  public void setApplicationStatus(ApplicationStatus value) {
    this.applicationStatus = value;
  }

  public RegistrationFeeRule getFeeRule() {
    return feeRule;
  }

  public void setFeeRule(RegistrationFeeRule value) {
    this.feeRule = value;
  }

  public BigDecimal getPayableAmount() {
    return payableAmount;
  }

  public void setPayableAmount(BigDecimal value) {
    this.payableAmount = value;
  }

  public LocalDate getPreviousValidUntil() {
    return previousValidUntil;
  }

  public void setPreviousValidUntil(LocalDate value) {
    this.previousValidUntil = value;
  }

  public Instant getSubmittedDate() {
    return submittedDate;
  }

  public void setSubmittedDate(Instant value) {
    this.submittedDate = value;
  }

  public Instant getUpdatedDate() {
    return updatedDate;
  }

  public void setUpdatedDate(Instant value) {
    this.updatedDate = value;
  }

  public String getVerificationRemark() {
    return verificationRemark;
  }

  public void setVerificationRemark(String value) {
    this.verificationRemark = value;
  }

  public RTOEmployee getVerifiedBy() {
    return verifiedBy;
  }

  public void setVerifiedBy(RTOEmployee value) {
    this.verifiedBy = value;
  }

  public Instant getVerifiedAt() {
    return verifiedAt;
  }

  public void setVerifiedAt(Instant value) {
    this.verifiedAt = value;
  }

  public LocalDate getInspectionScheduleDate() {
    return inspectionScheduleDate;
  }

  public void setInspectionScheduleDate(LocalDate value) {
    this.inspectionScheduleDate = value;
  }

  public String getInspectionRemark() {
    return inspectionRemark;
  }

  public void setInspectionRemark(String value) {
    this.inspectionRemark = value;
  }

  public RTOEmployee getInspectedBy() {
    return inspectedBy;
  }

  public void setInspectedBy(RTOEmployee value) {
    this.inspectedBy = value;
  }

  public Instant getInspectedAt() {
    return inspectedAt;
  }

  public void setInspectedAt(Instant value) {
    this.inspectedAt = value;
  }

  public RTOEmployee getDecidedBy() {
    return decidedBy;
  }

  public void setDecidedBy(RTOEmployee value) {
    this.decidedBy = value;
  }

  public Instant getDecidedAt() {
    return decidedAt;
  }

  public void setDecidedAt(Instant value) {
    this.decidedAt = value;
  }

  public String getDecisionRemarks() {
    return decisionRemarks;
  }

  public void setDecisionRemarks(String value) {
    this.decisionRemarks = value;
  }

  public InspectionStatus getInspectionStatus() {
    return inspectionStatus;
  }

  public void setInspectionStatus(InspectionStatus value) {
    this.inspectionStatus = value;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long value) {
    this.version = value;
  }
}
