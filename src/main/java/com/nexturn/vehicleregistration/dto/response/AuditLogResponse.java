package com.nexturn.vehicleregistration.dto.response;

import java.time.Instant;

/** Returns recorded workflow activity and timestamps for audit queries. */
public record AuditLogResponse(
    Long id,
    String actor,
    String action,
    String applicationRefNo,
    String remarks,
    Instant createdAt) {}
