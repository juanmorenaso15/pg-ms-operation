package com.pulse_gym.ms_operation.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.pulse_gym.lb_common.entity.operation.Equipo;
import com.pulse_gym.lb_common.enums.EnumEstado;

public interface EquipoRepository extends JpaRepository<Equipo, Long>,
        JpaSpecificationExecutor<Equipo> {

    /**
     * Busca un equipo por el número de serie
     * 
     * @param numeroSerie Número de serie del equipo
     * @return Optional con el equipo encontrado
     */
    Optional<Equipo> findByNumeroSerie(String numeroSerie);

    /**
     * Cuenta la cantidad de equipos con un estado específico
     * 
     * @param estado Estado del equipo (DISPONIBLE, MANTENIMIENTO, REPARACION)
     * @return Cantidad de equipos en el estado indicado
     */
    long countByEstado(EnumEstado estado);
}
