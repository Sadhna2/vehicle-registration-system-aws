package com.nexturn.vehicleregistration.exception;

import java.util.Map;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * Returns structured JSON errors for account and vehicle registration operations.
 * Preserves the previous backend ProblemDetail contract and error codes.
 * Unexpected errors return a generic message without database details.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private ResponseEntity<ProblemDetail> problem(HttpStatusCode status, String code, String detail) {
    ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail);
    body.setProperty("code", code);
    return ResponseEntity.status(status).body(body);
  }

  /** Translates the business errors used by registration and login. */
  @ExceptionHandler({DuplicateRecordException.class, InvalidLoginException.class, PasswordHashException.class,
      OwnerNotFoundException.class, ReferenceNumberNotFoundException.class,
      ApplicationIdNotFoundException.class, FeeRuleNotFoundException.class, InvalidOperationException.class,
      EmployeeNotFoundException.class, InvalidRequestException.class,
      PaymentRequiredException.class, PaymentAmountMismatchException.class,
      InspectionNotPassedException.class, RegistrationCertificateNotFoundException.class})
  public ResponseEntity<ProblemDetail> applicationError(RuntimeException error) {
    ApiError details = (ApiError) error;
    if (details.getStatus().is5xxServerError())
      LOG.error("Backend operation failed: {}", details.getCode(), error);
    return problem(details.getStatus(), details.getCode(), error.getMessage());
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ProblemDetail> status(ResponseStatusException error) {
    return problem(
        error.getStatusCode(),
        "REQUEST_REJECTED",
        error.getReason() == null ? "Request rejected" : error.getReason());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> validation(MethodArgumentNotValidException error) {
    var fields =
        error.getBindingResult().getFieldErrors().stream()
            .map(
                field ->
                    Map.of(
                        "field",
                        field.getField(),
                        "message",
                        field.getDefaultMessage() == null
                            ? "Invalid value"
                            : field.getDefaultMessage()))
            .toList();
    ProblemDetail body =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "Validation failed. Check the highlighted fields.");
    body.setProperty("code", "VALIDATION_FAILED");
    body.setProperty("errors", fields);
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MissingServletRequestParameterException.class,
    MethodArgumentTypeMismatchException.class,
    ConstraintViolationException.class,
    HandlerMethodValidationException.class
  })
  public ResponseEntity<ProblemDetail> malformed(Exception error) {
    return problem(
        HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Request contains missing or invalid values.");
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ProblemDetail> duplicate(DataIntegrityViolationException error) {
    return problem(
        HttpStatus.CONFLICT,
        "DUPLICATE_RECORD",
        "A unique value already exists or a related record is invalid.");
  }

  @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
  public ResponseEntity<ProblemDetail> missing(Exception error) {
    return problem(HttpStatus.NOT_FOUND, "NOT_FOUND", "Endpoint not found.");
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ProblemDetail> method(HttpRequestMethodNotSupportedException error) {
    return problem(
        HttpStatus.METHOD_NOT_ALLOWED,
        "METHOD_NOT_ALLOWED",
        "HTTP method is not supported for this endpoint.");
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ProblemDetail> media(HttpMediaTypeNotSupportedException error) {
    return problem(
        HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE", "This endpoint expects JSON.");
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> unexpected(Exception error) {
    LOG.error("Unexpected backend failure", error);
    return problem(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "INTERNAL_ERROR",
        "An unexpected error occurred. Try again later.");
  }
}
