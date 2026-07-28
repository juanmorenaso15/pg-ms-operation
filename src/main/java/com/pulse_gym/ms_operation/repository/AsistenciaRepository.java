package com.pulse_gym.ms_operation.repository;

import com.pulse_gym.lb_common.entity.operation.Asistencia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {
    
    /**
     * Busca las asistencias por el ID del usuario y las ordena por la fecha y hora de entrada de forma descendente
     * @param idUsuario
     * @return
     */
    List<Asistencia> findByIdUsuarioOrderByFechaHoraEntradaDesc(Long idUsuario);
    
    /**
     * Busca las asistencias por el ID del usuario y las página
     * @param idUsuario
     * @param pageable
     * @return Page<Asistencia> con las asistencias encontradas
     */
    Page<Asistencia> findByIdUsuario(Long idUsuario, Pageable pageable);
    /**
     * Busca las asistencias por el rango de fechas
     * @param inicio
     * @param fin
     * @return List<Asistencia> con las asistencias encontradas
     */
    List<Asistencia> findByFechaHoraEntradaBetween(LocalDateTime inicio, LocalDateTime fin);
    
    /**
     * Busca las asistencias por el ID de la sede y las ordena por la fecha y hora de entrada de forma descendente
     * @param idSede
     * @return List<Asistencia> con las asistencias encontradas
     */
    List<Asistencia> findBySedeIdSedeOrderByFechaHoraEntradaDesc(Long idSede);
    
    /**
     * Cuenta las asistencias permitidas por el ID del usuario y el rango de fechas
     * @param idUsuario
     * @param inicio
     * @param fin
     * @return Long con el número de asistencias permitidas
     */
    @Query("SELECT COUNT(a) FROM Asistencia a WHERE a.idUsuario = :idUsuario AND a.estadoAcceso = 'PERMITIDO' AND a.fechaHoraEntrada BETWEEN :inicio AND :fin")
    Long countAsistenciasPermitidas(@Param("idUsuario") Long idUsuario, 
                                    @Param("inicio") LocalDateTime inicio, 
                                    @Param("fin") LocalDateTime fin);
    
    /**
     * Busca la última asistencia por el ID del usuario y las página
     * @param idUsuario
     * @param pageable
     * @return List<Asistencia> con la última asistencia encontrada
     */
    @Query("SELECT a FROM Asistencia a WHERE a.idUsuario = :idUsuario ORDER BY a.fechaHoraEntrada DESC")
    List<Asistencia> findLastAsistenciaByUsuario(@Param("idUsuario") Long idUsuario, Pageable pageable);
}