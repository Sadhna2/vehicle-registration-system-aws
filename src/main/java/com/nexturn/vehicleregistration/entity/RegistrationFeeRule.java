package com.nexturn.vehicleregistration.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.nexturn.vehicleregistration.enums.ApplicationType;
import com.nexturn.vehicleregistration.enums.VehicleCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Maps category-specific registration fees and their effective dates. */
@Entity
@Table(name = "registration_fee_rules")
public class RegistrationFeeRule {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long feeRuleId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private VehicleCategory vehicleCategory;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ApplicationType applicationType;

  @Column(precision = 10, scale = 2, nullable = false)
  private BigDecimal feeAmount;

  @Column(nullable = false)
  private LocalDate effectiveFrom;

  public Long getFeeRuleId() {
    return feeRuleId;
  }

  public void setFeeRuleId(Long value) {
    this.feeRuleId = value;
  }

  public VehicleCategory getVehicleCategory() {
    return vehicleCategory;
  }

  public void setVehicleCategory(VehicleCategory value) {
    this.vehicleCategory = value;
  }

  public ApplicationType getApplicationType() {
    return applicationType;
  }

  public void setApplicationType(ApplicationType value) {
    this.applicationType = value;
  }

  public BigDecimal getFeeAmount() {
    return feeAmount;
  }

  public void setFeeAmount(BigDecimal value) {
    this.feeAmount = value;
  }

  public LocalDate getEffectiveFrom() {
    return effectiveFrom;
  }

  public void setEffectiveFrom(LocalDate value) {
    this.effectiveFrom = value;
  }
}
