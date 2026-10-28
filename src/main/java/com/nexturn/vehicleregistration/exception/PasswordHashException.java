package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

/**
 * Reports a password hashing failure with HTTP 500.
 */
public class PasswordHashException extends RuntimeException implements ApiError {
  private static final long serialVersionUID = 1L;
  public PasswordHashException(Throwable cause) {
    super("Password hashing is unavailable.", cause);
  }

  public HttpStatus getStatus() {
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }

  public String getCode() {
    return "PASSWORD_HASH_FAILED";
  }
}
