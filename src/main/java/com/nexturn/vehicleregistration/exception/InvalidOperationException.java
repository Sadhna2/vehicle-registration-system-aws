package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

/** Returns a named business failure and its existing HTTP status and error code. */
public class InvalidOperationException extends RuntimeException implements ApiError {
  private static final long serialVersionUID = 1L;
  public InvalidOperationException(String message) {
    super(message);
  }

  public HttpStatus getStatus() {
    return HttpStatus.CONFLICT;
  }

  public String getCode() {
    return "INVALID_OPERATION";
  }
}
