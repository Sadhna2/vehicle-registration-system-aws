package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

/** Returns a named business failure and its existing HTTP status and error code. */
public class OwnerNotFoundException extends RuntimeException implements ApiError {
  private static final long serialVersionUID = 1L;
  public OwnerNotFoundException(Long ownerId) {
    super("Owner not found: " + ownerId);
  }

  public HttpStatus getStatus() {
    return HttpStatus.NOT_FOUND;
  }

  public String getCode() {
    return "OWNER_NOT_FOUND";
  }
}
