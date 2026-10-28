package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

/** Returns a named business failure and its existing HTTP status and error code. */
public class FeeRuleNotFoundException extends RuntimeException implements ApiError {
  private static final long serialVersionUID = 1L;
  public FeeRuleNotFoundException() {
    super("No registration fee is configured for this vehicle category.");
  }

  public HttpStatus getStatus() {
    return HttpStatus.NOT_FOUND;
  }

  public String getCode() {
    return "FEE_RULE_NOT_FOUND";
  }
}
