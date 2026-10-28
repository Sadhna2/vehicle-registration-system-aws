package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

/** Preserves the existing named business exception, HTTP status and stable error code. */
public class PaymentAmountMismatchException extends RuntimeException implements ApiError {
  private static final long serialVersionUID = 1L;
  public PaymentAmountMismatchException() {
    super("Payment amount must equal the quoted registration fee.");
  }

  public HttpStatus getStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  public String getCode() {
    return "PAYMENT_AMOUNT_MISMATCH";
  }
}
