package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.entity.VehicleRegistrationApplication;

/** Defines audit recording for registration workflow changes. */
public interface AuditService {
    /** Records a workflow change in the current transaction; application may be null for fee setup. */
    void record(
            VehicleRegistrationApplication application,
            String action,
            String remarks);
}
