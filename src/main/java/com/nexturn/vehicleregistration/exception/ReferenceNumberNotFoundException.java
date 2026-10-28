package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

/** Returns a named business failure and its existing HTTP status and error code. */
public class ReferenceNumberNotFoundException extends RuntimeException implements ApiError {
  private static final long serialVersionUID = 1L;
  public ReferenceNumberNotFoundException(String referenceNumber) {
    super("Application reference number not found: " + referenceNumber);
  }

  public HttpStatus getStatus() {
    return HttpStatus.NOT_FOUND;
  }

  public String getCode() {
    return "REFERENCE_NUMBER_NOT_FOUND";
  }
}
