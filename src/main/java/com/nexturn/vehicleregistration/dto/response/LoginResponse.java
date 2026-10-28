package com.nexturn.vehicleregistration.dto.response;

/**
 * Returns account identifier, display name and frontend account type.
 * OWNER is a response label, not a role column in the owner table.
 * Contains no password or authentication token.
 *
 * @param id persistent account identifier
 * @param name account first name for display
 * @param role OWNER for an owner, or the stored employee role
 */
public record LoginResponse(Long id, String name, String role) {}
