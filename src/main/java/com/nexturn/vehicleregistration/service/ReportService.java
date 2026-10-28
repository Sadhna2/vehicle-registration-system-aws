package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.dto.response.AuditLogResponse;

import java.util.List;
import java.util.Map;

/** Defines registration summary and audit-history queries. */
public interface ReportService {
    Map<String, Object> report();

    List<AuditLogResponse> audits();
}
