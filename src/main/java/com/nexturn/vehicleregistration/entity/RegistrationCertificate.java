package com.nexturn.vehicleregistration.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/** Maps the existing certificate table and its application, issuing employee and registered owner relationships. */
@Entity
@Table(name = "registration_certificate")
public class RegistrationCertificate {
  @Id
  @Column(length = 20)
  private String registrationNumber;

  @OneToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "temporaryregistration_no", unique = true)
  private VehicleRegistrationApplication application;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "issued_by")
  private RTOEmployee issuedBy;

  @Column(nullable = false)
  private LocalDate issuedDate;

  @Column(nullable = false)
  private LocalDate validTill;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "register_owner_id")
  private Owner registeredOwner;

  public String getRegistrationNumber() {
    return registrationNumber;
  }

  public void setRegistrationNumber(String value) {
    this.registrationNumber = value;
  }

  public VehicleRegistrationApplication getApplication() {
    return application;
  }

  public void setApplication(VehicleRegistrationApplication value) {
    this.application = value;
  }

  public RTOEmployee getIssuedBy() {
    return issuedBy;
  }

  public void setIssuedBy(RTOEmployee value) {
    this.issuedBy = value;
  }

  public LocalDate getIssuedDate() {
    return issuedDate;
  }

  public void setIssuedDate(LocalDate value) {
    this.issuedDate = value;
  }

  public LocalDate getValidTill() {
    return validTill;
  }

  public void setValidTill(LocalDate value) {
    this.validTill = value;
  }

  public Owner getRegisteredOwner() {
    return registeredOwner;
  }

  public void setRegisteredOwner(Owner value) {
    this.registeredOwner = value;
  }
}
