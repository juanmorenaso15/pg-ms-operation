package com.pulse_gym.ms_operation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pulse_gym.lb_common.entity.operation.AuditoriaBiometrica;

public interface AuditoriaBiometricaRepository extends JpaRepository<AuditoriaBiometrica, Long> {
}