package com.pulse_gym.ms_operation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pulse_gym.lb_common.entity.operation.Sede;
import java.util.List;
import java.util.Optional;

@Repository
public interface SedeRepository extends JpaRepository<Sede, Long> {
    
    /**
     * Busca una sede por su nombre (por completo).
     * @param nombreSede
     * @return Optional<Sede> con la sede encontrada
     */
    Optional<Sede> findByNombreSede(String nombreSede);
    
    /**
     * Busca sedes por su nombre (por completo).
     * @param nombreSede
     * @return List<Sede> con las sedes encontradas     
     */
    List<Sede> findByNombreSedeContainingIgnoreCase(String nombreSede);
    
    /**
     * Busca sedes por su ciudad.
     * @param ciudad
     * @return List<Sede> con las sedes encontradas  
     */
    List<Sede> findByCiudadContainingIgnoreCase(String ciudad);
    
    /**
     * Verifica si existe una sede por su nombre (por completo).
     * @param nombreSede
     * @return boolean true si existe, false si no existe
     */
    boolean existsByNombreSede(String nombreSede);
    
    /**         
     * Busca todas las sedes paginadas.
     * @param pageable
     * @return Page<Sede> con las sedes encontradas
     */
    Page<Sede> findAll(Pageable pageable);
    
    /**
     * Cuenta los equipos asociados a una sede.
     * @param idSede
     * @return Long con el número de equipos asociados a la sede
     */
    @Query("SELECT COUNT(e) FROM Equipo e WHERE e.sede.idSede = :idSede")
    Long countEquiposBySedeId(@Param("idSede") Long idSede);
}