package com.pulse_gym.ms_operation.controller;

import com.pulse_gym.lb_common.dto.HistorialAccesoDTO;
import com.pulse_gym.lb_common.dto.HistorialAccesoFiltroDTO;
import com.pulse_gym.ms_operation.services.HistorialAccesoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/historial-accesos")
@RequiredArgsConstructor
public class HistorialAccesoController {

    private final HistorialAccesoService historialAccesoService;

    /**
     * Consulta el historial de accesos con filtros opcionales y paginación.
     * Solo administradores.
     *
     * @param usuarioId   ID del usuario (opcional)
     * @param fechaInicio Fecha de inicio (opcional, formato ISO)
     * @param fechaFin    Fecha de fin (opcional)
     * @param tipoAcceso  Tipo de acceso: WEB, HUELLA (opcional)
     * @param resultado   Resultado: EXITOSO, FALLIDO, BLOQUEADO (opcional)
     * @param pageable    Paginación (page, size, sort)
     * @param userRol     Rol del usuario (header)
     * @return Página de historial de accesos
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> consultarHistorialAccesos(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(required = false) String tipoAcceso,
            @RequestParam(required = false) String resultado,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {

        HistorialAccesoFiltroDTO filtro = new HistorialAccesoFiltroDTO();
        filtro.setUsuarioId(usuarioId);
        filtro.setFechaInicio(fechaInicio);
        filtro.setFechaFin(fechaFin);
        filtro.setTipoAcceso(tipoAcceso);
        filtro.setResultado(resultado);

        Page<HistorialAccesoDTO> page = historialAccesoService.consultarHistorialAccesos(filtro, pageable, userRol);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        if (page.getContent().isEmpty()) {
            response.put("message", "No se encontraron registros");
        } else {
            response.put("message", "Consulta exitosa");
        }
        response.put("content", page.getContent());
        response.put("totalElements", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        response.put("currentPage", page.getNumber());
        response.put("size", page.getSize());
        response.put("first", page.isFirst());
        response.put("last", page.isLast());

        return ResponseEntity.ok(response);
    }
}