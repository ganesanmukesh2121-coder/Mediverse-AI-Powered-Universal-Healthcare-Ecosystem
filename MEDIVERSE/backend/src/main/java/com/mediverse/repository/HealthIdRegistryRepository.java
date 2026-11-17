package com.mediverse.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mediverse.entity.HealthIdRegistry;

@Repository
public interface HealthIdRegistryRepository extends JpaRepository<HealthIdRegistry, Long> {
    boolean existsByHealthId(String healthId);
    Optional<HealthIdRegistry> findByHealthId(String healthId);
}
