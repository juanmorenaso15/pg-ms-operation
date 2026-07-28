package com.pulse_gym.ms_operation.repository;

import com.pulse_gym.lb_common.dto.HistorialAccesoDTO;
import com.pulse_gym.lb_common.dto.HistorialAccesoFiltroDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HistorialAccesoRepositoryCustom {

    /**
     * Consulta el historial de accesos combinando las tablas asistencia y auditoria_biometrica.
     * Aplica los filtros y devuelve una página paginada.
     *
     * @param filtro   Filtros de búsqueda
     * @param pageable Paginación
     * @return Página de HistorialAccesoDTO
     */
    Page<HistorialAccesoDTO> consultarHistorialAccesos(HistorialAccesoFiltroDTO filtro, Pageable pageable);
}