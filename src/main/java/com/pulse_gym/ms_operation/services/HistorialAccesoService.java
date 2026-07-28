package com.pulse_gym.ms_operation.services;

import com.pulse_gym.lb_common.dto.HistorialAccesoDTO;
import com.pulse_gym.lb_common.dto.HistorialAccesoFiltroDTO;
import com.pulse_gym.lb_common.services.ValidacionDeRoles;
import com.pulse_gym.ms_operation.repository.HistorialAccesoRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HistorialAccesoService {

    private final HistorialAccesoRepositoryCustom historialAccesoRepository;

    /**
     * Consulta el historial de accesos con filtros y paginación.
     * Solo accesible para administradores.
     *
     * @param filtro   Filtros de búsqueda
     * @param pageable Paginación
     * @param userRol  Rol del usuario autenticado
     * @return Página de historial de accesos
     */
    public Page<HistorialAccesoDTO> consultarHistorialAccesos(HistorialAccesoFiltroDTO filtro, Pageable pageable, String userRol) {
        ValidacionDeRoles.validarAdmin(userRol);
        return historialAccesoRepository.consultarHistorialAccesos(filtro, pageable);
    }
}