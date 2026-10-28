package com.nexturn.vehicleregistration.entity;

import java.time.LocalDate;

import com.nexturn.vehicleregistration.enums.FuelType;
import com.nexturn.vehicleregistration.enums.VehicleCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Maps the existing vehicle_details table without changing its column names. */
@Entity
@Table(name = "vehicle_details")
public class Vehicle {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "temporaryregister_no")
  private Long temporaryregisterNo;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "current_owner_id")
  private Owner currentOwner;

  @Enumerated(EnumType.STRING)
  @Column(name = "vechile_category", nullable = false)
  private VehicleCategory vehicleCategory;

  @Column(name = "manufactor_name", length = 50, nullable = false)
  private String manufacturerName;

  @Column(name = "model_name", length = 50, nullable = false)
  private String modelName;

  @Column(nullable = false, unique = true, length = 50)
  private String chassisNumber;

  @Column(nullable = false, unique = true, length = 50)
  private String engineNumber;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private FuelType fuelType;

  @Column(nullable = false)
  private int manufactureYear;

  @Column(unique = true, length = 20)
  private String registrationcertificateNumber;

  private LocalDate firstRegistrationDate;
  private LocalDate registrationValidTill;

  @Column(length = 30)
  private String colorVariant;

  public Long getTemporaryregisterNo() {
    return temporaryregisterNo;
  }

  public void setTemporaryregisterNo(Long value) {
    this.temporaryregisterNo = value;
  }

  public Owner getCurrentOwner() {
    return currentOwner;
  }

  public void setCurrentOwner(Owner value) {
    this.currentOwner = value;
  }

  public VehicleCategory getVehicleCategory() {
    return vehicleCategory;
  }

  public void setVehicleCategory(VehicleCategory value) {
    this.vehicleCategory = value;
  }

  public String getManufacturerName() {
    return manufacturerName;
  }

  public void setManufacturerName(String value) {
    this.manufacturerName = value;
  }

  public String getModelName() {
    return modelName;
  }

  public void setModelName(String value) {
    this.modelName = value;
  }

  public String getChassisNumber() {
    return chassisNumber;
  }

  public void setChassisNumber(String value) {
    this.chassisNumber = value;
  }

  public String getEngineNumber() {
    return engineNumber;
  }

  public void setEngineNumber(String value) {
    this.engineNumber = value;
  }

  public FuelType getFuelType() {
    return fuelType;
  }

  public void setFuelType(FuelType value) {
    this.fuelType = value;
  }

  public int getManufactureYear() {
    return manufactureYear;
  }

  public void setManufactureYear(int value) {
    this.manufactureYear = value;
  }

  public String getRegistrationcertificateNumber() {
    return registrationcertificateNumber;
  }

  public void setRegistrationcertificateNumber(String value) {
    this.registrationcertificateNumber = value;
  }

  public LocalDate getFirstRegistrationDate() {
    return firstRegistrationDate;
  }

  public void setFirstRegistrationDate(LocalDate value) {
    this.firstRegistrationDate = value;
  }

  public LocalDate getRegistrationValidTill() {
    return registrationValidTill;
  }

  public void setRegistrationValidTill(LocalDate value) {
    this.registrationValidTill = value;
  }

  public String getColorVariant() {
    return colorVariant;
  }

  public void setColorVariant(String value) {
    this.colorVariant = value;
  }
}
