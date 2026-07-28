// MantenimientoRepository.java
package com.pulse_gym.ms_operation.repository;

import com.pulse_gym.lb_common.entity.operation.Mantenimiento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MantenimientoRepository extends JpaRepository<Mantenimiento, Long>, 
                                                JpaSpecificationExecutor<Mantenimiento> {
    
    /**
     * Busca los mantenimientos por el ID del equipo
     * @param idEquipo
     * @return List<Mantenimiento> con los mantenimientos encontrados
     */
    List<Mantenimiento> findByEquipoIdEquipo(Long idEquipo);
    
    
    /**
     * Busca los mantenimientos por el ID del equipo y los ordena por la fecha de servicio de forma descendente
     * @param idEquipo
     * @return List<Mantenimiento> con los mantenimientos encontrados
     */
    List<Mantenimiento> findByEquipoIdEquipoOrderByFechaServicioDesc(Long idEquipo);
    
    /**
     * Busca los 5 últimos mantenimientos por el ID del equipo y los ordena por la fecha de servicio de forma descendente
     * @param idEquipo
     * @return List<Mantenimiento> con los 5 últimos mantenimientos encontrados
     */
    List<Mantenimiento> findTop5ByEquipoIdEquipoOrderByFechaServicioDesc(Long idEquipo);
    
    
    /**
     * Cuenta los mantenimientos por el ID del equipo
     * @param idEquipo
     * @return Long con el número de mantenimientos encontrados
     */
    Long countByEquipoIdEquipo(Long idEquipo);
}