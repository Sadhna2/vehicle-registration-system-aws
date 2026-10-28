package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

/** Preserves the existing named business exception, HTTP status and stable error code. */
public class InvalidRequestException extends RuntimeException implements ApiError {
  private static final long serialVersionUID = 1L;
  public InvalidRequestException(String message) {
    super(message);
  }

  public HttpStatus getStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  public String getCode() {
    return "INVALID_REQUEST";
  }
}
