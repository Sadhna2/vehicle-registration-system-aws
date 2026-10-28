package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

/** Preserves the existing named business exception, HTTP status and stable error code. */
public class InspectionNotPassedException extends RuntimeException implements ApiError {
  private static final long serialVersionUID = 1L;
  public InspectionNotPassedException() {
    super("A passed inspection is required before approval.");
  }

  public HttpStatus getStatus() {
    return HttpStatus.CONFLICT;
  }

  public String getCode() {
    return "INSPECTION_NOT_PASSED";
  }
}
