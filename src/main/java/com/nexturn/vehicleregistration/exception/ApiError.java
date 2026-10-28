package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

/**
 * Defines the HTTP status and stable code returned for a business error.
 */
public interface ApiError {
  HttpStatus getStatus();
  String getCode();
}
