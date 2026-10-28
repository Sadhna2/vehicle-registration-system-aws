package com.nexturn.vehicleregistration.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexturn.vehicleregistration.entity.RegistrationCertificate;

/** Retrieves the certificate issued for an approved application. */
public interface RegistrationCertificateRepository extends JpaRepository<RegistrationCertificate, String> {
    Optional<RegistrationCertificate> findByApplicationApplicationRefNo(String ref);
}
