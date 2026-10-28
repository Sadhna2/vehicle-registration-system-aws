package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.exception.InvalidOperationException;

/** Shares registration business-rule validation between service implementations. */
public final class WorkflowSupport {
    private WorkflowSupport() { }

    /** Rejects a transition or value that conflicts with the registration rules. */
    public static void ensure(boolean condition, String message) {
        if (!condition) {
            throw new InvalidOperationException(message);
        }
    }
}
