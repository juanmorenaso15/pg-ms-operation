package com.pulse_gym.ms_operation.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pulse_gym.lb_common.dto.HistorialMantenimientoDTO;
import com.pulse_gym.lb_common.dto.MantenimientoRequestDTO;
import com.pulse_gym.lb_common.dto.MessegeGlobalDTO;
import com.pulse_gym.ms_operation.services.MantenimientoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/mantenimientos")
@RequiredArgsConstructor
public class MantenimientoController {

    /**
     * Inyeccion de MantenimientoService para manejar la lógica de negocio relacionada con
     * los mantenimientos, como el registro y la obtención de mantenimientos.
     */
    private final MantenimientoService mantenimientoService;

    /**
     * Endpoint para registrar un nuevo mantenimiento. Recibe un objeto MantenimientoRequestDTO en
     * el cuerpo de la solicitud, y registra el mantenimiento en la base de datos.
     * @param mantenimientoRequestDTO
     * @param userRol Rol del usuario que hace la petición (desde header X-User-Rol)
     * @return ResponseEntity<MessegeGlobalDTO> con el resultado del registro
     */
    @PostMapping
    public ResponseEntity<MessegeGlobalDTO> registrarMantenimiento(@Valid @RequestBody MantenimientoRequestDTO mantenimientoRequestDTO, 
                                                                    @RequestHeader(value = "X-User-Rol", required = false) String userRol
    ) {

        try {
            MessegeGlobalDTO response = mantenimientoService.registrarMantenimiento(mantenimientoRequestDTO, userRol);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
            
        } catch (Exception e) {
            e.printStackTrace();

            MessegeGlobalDTO dto = new MessegeGlobalDTO(e.getMessage());

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(dto);
        }
    }

    /**
     * Endpoint para obtener el historial de mantenimientos de un equipo. Recibe el ID del equipo como
     * parte de la URL, y obtiene el historial de mantenimientos de ese equipo.
     * @param idEquipo
     * @param userRol Rol del usuario que hace la petición (desde header X-User-Rol)
     * @return ResponseEntity<Map<String, Object>> con el historial de mantenimientos
     */
    @GetMapping("/historial/equipo/{idEquipo}")
    public ResponseEntity<Map<String, Object>> obtenerHistorialPorEquipo(@PathVariable Long idEquipo,
                                                                         @RequestHeader(value = "X-User-Rol", required = false) String userRol)
    {

        try {
            List<HistorialMantenimientoDTO> historial = mantenimientoService.obtenerHistorialPorEquipo(idEquipo, userRol);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Historial de mantenimientos encontrado");
            response.put("count", historial.size());
            response.put("data", historial);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al consultar historial: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
