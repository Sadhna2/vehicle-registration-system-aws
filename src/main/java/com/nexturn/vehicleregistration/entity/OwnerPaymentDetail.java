package com.nexturn.vehicleregistration.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.nexturn.vehicleregistration.enums.PaymentMethod;
import com.nexturn.vehicleregistration.enums.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/** Maps the existing payment table; stores the last four digits and a simulated transaction reference only. */
@Entity
@Table(name = "owner_payment_detail")
public class OwnerPaymentDetail {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long paymentId;

  @OneToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "application_id", unique = true)
  private VehicleRegistrationApplication application;

  @Column(precision = 10, scale = 2, nullable = false)
  private BigDecimal amountPaid;

  @Column(length = 4, nullable = false)
  private String cardLastFourDigit;

  @Column(unique = true, length = 50, nullable = false)
  private String transactionReferenceId;

  @Enumerated(EnumType.STRING)
  @Column(length = 20, nullable = false)
  private PaymentStatus paymentStatus = PaymentStatus.SUCCESS;

  private Instant paidAt = Instant.now();

  @Enumerated(EnumType.STRING)
  @Column(length = 20, nullable = false)
  private PaymentMethod paymentMethod = PaymentMethod.CARD;

  public Long getPaymentId() {
    return paymentId;
  }

  public void setPaymentId(Long value) {
    this.paymentId = value;
  }

  public VehicleRegistrationApplication getApplication() {
    return application;
  }

  public void setApplication(VehicleRegistrationApplication value) {
    this.application = value;
  }

  public BigDecimal getAmountPaid() {
    return amountPaid;
  }

  public void setAmountPaid(BigDecimal value) {
    this.amountPaid = value;
  }

  public String getCardLastFourDigit() {
    return cardLastFourDigit;
  }

  public void setCardLastFourDigit(String value) {
    this.cardLastFourDigit = value;
  }

  public String getTransactionReferenceId() {
    return transactionReferenceId;
  }

  public void setTransactionReferenceId(String value) {
    this.transactionReferenceId = value;
  }

  public PaymentStatus getPaymentStatus() {
    return paymentStatus;
  }

  public void setPaymentStatus(PaymentStatus value) {
    this.paymentStatus = value;
  }

  public Instant getPaidAt() {
    return paidAt;
  }

  public void setPaidAt(Instant value) {
    this.paidAt = value;
  }

  public PaymentMethod getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(PaymentMethod value) {
    this.paymentMethod = value;
  }
}
