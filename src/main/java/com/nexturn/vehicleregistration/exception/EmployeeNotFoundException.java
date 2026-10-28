package com.nexturn.vehicleregistration.exception;

import org.springframework.http.HttpStatus;

/** Preserves the existing named business exception, HTTP status and stable error code. */
public class EmployeeNotFoundException extends RuntimeException implements ApiError {
  private static final long serialVersionUID = 1L;
  public EmployeeNotFoundException(Long employeeId) {
    super("Employee not found: " + employeeId);
  }

  public HttpStatus getStatus() {
    return HttpStatus.NOT_FOUND;
  }

  public String getCode() {
    return "EMPLOYEE_NOT_FOUND";
  }
}
