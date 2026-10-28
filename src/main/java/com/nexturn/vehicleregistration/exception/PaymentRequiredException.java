package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

/** Preserves the existing named business exception, HTTP status and stable error code. */
public class PaymentRequiredException extends RuntimeException implements ApiError {
  private static final long serialVersionUID = 1L;
  public PaymentRequiredException() {
    super("A successful payment is required before approval.");
  }

  public HttpStatus getStatus() {
    return HttpStatus.CONFLICT;
  }

  public String getCode() {
    return "PAYMENT_REQUIRED";
  }
}
