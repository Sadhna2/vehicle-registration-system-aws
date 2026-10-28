package com.nexturn.vehicleregistration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.nexturn.vehicleregistration.entity.AuditLogs;

/** Persists audit entries within the enclosing registration transaction. */
public interface AuditLogRepository extends JpaRepository<AuditLogs, Long> {
}
