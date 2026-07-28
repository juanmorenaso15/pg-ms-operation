package com.pulse_gym.ms_operation.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pulse_gym.lb_common.dto.ActualizarEstadoReporteDTO;
import com.pulse_gym.lb_common.dto.ConsultaEquipoRequestDTO;
import com.pulse_gym.lb_common.dto.EquipoRequestDTO;
import com.pulse_gym.lb_common.dto.EstadoEquipoRequestDTO;
import com.pulse_gym.lb_common.dto.MessegeGlobalDTO;
import com.pulse_gym.lb_common.dto.ReporteFallaDTO;
import com.pulse_gym.lb_common.entity.operation.Equipo;
import com.pulse_gym.ms_operation.services.EquipoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/equipos")
@RequiredArgsConstructor
public class EquipoController {

    /**
     * Inyeccion de EquipoService para manejar la lógica de negocio relacionada con
     * los equipos, como el registro y la obtención de equipos.
     */
    private final EquipoService equipoService;

    /**
     * Endpoint para registrar un nuevo equipo. Recibe un objeto EquipoRequestDTO en
     * el cuerpo de la solicitud
     * 
     * @param equipoRequestDTO
     * @param userRol Rol del usuario que hace la petición (desde header X-User-Rol)
     * @return ResponseEntity<MessegeGlobalDTO>
     */
    @PostMapping
    public ResponseEntity<MessegeGlobalDTO> registrarEquipo(@Valid @RequestBody EquipoRequestDTO equipoRequestDTO, 
                                                            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {
        try {
            MessegeGlobalDTO response = equipoService.registrarEquipo(equipoRequestDTO, userRol);
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
     * Endpoint para consultar equipos segun los datos pasados por el cuerpo de la
     * solicitud
     * 
     * @param request
     * @param userRol Rol del usuario que hace la petición (desde header X-User-Rol)
     * @return ResponseEntity<Map<String, Object>> con la respuesta de la consulta
     */
    @PostMapping("/consultar")
    public ResponseEntity<Map<String, Object>> consultarEquipos(@RequestBody ConsultaEquipoRequestDTO request,
                                                                @RequestHeader(value = "X-User-Rol", required = false) String userRol   
    ) {
        try {
            List<Equipo> equipos = equipoService.obtenerEquipos(request, userRol);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);

            // Mensaje diferenciado según si hay resultados o no
            if (equipos.isEmpty()) {
                response.put("message", "Consulta exitosa, no se encontraron equipos");
                response.put("count", 0);
                response.put("data", equipos);
            } else {
                response.put("message", "Consulta exitosa");
                response.put("count", equipos.size());
                response.put("data", equipos);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al consultar equipos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Endpoint para actualizar un equipo existente. Recibe el ID del equipo a
     * actualizar como parte de la URL y
     * un objeto EquipoRequestDTO con los nuevos datos en el cuerpo de la solicitud.
     * 
     * @param id
     * @param equipoRequestDTO
     * @return ResponseEntity<MessegeGlobalDTO> con el resultado de la actualización
     */
    @PutMapping("/{id}")
    public ResponseEntity<MessegeGlobalDTO> actualizarEquipo(@PathVariable Long id,
                                                            @Valid @RequestBody EquipoRequestDTO equipoRequestDTO,
                                                            @RequestHeader(value = "X-User-Rol", required = false) String userRol
    ) {
        try {
            MessegeGlobalDTO response = equipoService.actualizarEquipo(id, equipoRequestDTO, userRol);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            MessegeGlobalDTO dto = new MessegeGlobalDTO(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(dto);
        }
    }

    /**
     * Endpoint para cambiar el estado de un equipo. Recibe el ID del equipo como
     * parte de la URL, un objeto EstadoEquipoRequestDTO con el nuevo estado en el cuerpo de la solicitud 
     * y el rol del usuario que hace la petición.
     * 
     * @param id
     * @param estadoRequestDTO
     * @param userRol Rol del usuario que hace la petición (desde header X-User-Rol)
     * @return ResponseEntity<MessegeGlobalDTO> con el resultado del cambio de
     *         estado
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<MessegeGlobalDTO> cambiarEstadoEquipo(@PathVariable Long id,
                                                                @Valid @RequestBody EstadoEquipoRequestDTO estadoRequestDTO,
                                                                @RequestHeader(value = "X-User-Rol", required = false) String userRol ) 
    {
        try {
            MessegeGlobalDTO response = equipoService.cambiarEstadoEquipo(id, estadoRequestDTO, userRol);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            MessegeGlobalDTO dto = new MessegeGlobalDTO(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(dto);
        }
    }

    /**
     * Endpoint para reportar una falla en un equipo. Recibe el ID del equipo como
     * parte de la URL y un objeto ReporteFallaDTO con los datos de la falla en el cuerpo de la solicitud
     * y el rol del usuario que hace la petición.
     * @param idEquipo
     * @param request
     * @param userRol Rol del usuario que hace la petición (desde header X-User-Rol)
     * @return ResponseEntity<Map<String, Object>> con el resultado del reporte de la falla
     */
    @PostMapping("/{idEquipo}/reportar-falla")
    public ResponseEntity<Map<String, Object>> reportarFalla(@PathVariable Long idEquipo,
                                                            @Valid @RequestBody ReporteFallaDTO request,
                                                            @RequestHeader(value = "X-User-Rol", required = false) String userRol )
    {
        try {
            MessegeGlobalDTO response = equipoService.reportarFalla(idEquipo, request, userRol);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", response.getMessage());

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Endpoint para actualizar el estado de un reporte de falla. Recibe el ID del equipo como
     * parte de la URL y un objeto ActualizarEstadoReporteDTO con el nuevo estado en el cuerpo de la solicitud.
     * 
     * Se valida que la ruta solo pueda hacer la peticion un Admin, un Entrenador o un Recepcionista
     * 
     * @param idEquipo
     * @param request
     * @param userRol Rol del usuario que hace la petición (desde header X-User-Rol)
     * @return ResponseEntity<Map<String, Object>> con el resultado de la actualización del estado del reporte de falla
     */
    @PatchMapping("/{idEquipo}/estado-reporte")
    public ResponseEntity<Map<String, Object>> actualizarEstadoReporte(@PathVariable Long idEquipo,
                                                                        @Valid @RequestBody ActualizarEstadoReporteDTO request,
                                                                        @RequestHeader(value = "X-User-Rol", required = false) String userRol)
    {
        try {
            MessegeGlobalDTO response = equipoService.actualizarEstadoReporte(idEquipo, request, userRol);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", response.getMessage());

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Endpoint para consultar reportes de falla. Recibe el ID del equipo, el estado y la urgencia como parte de la URL y los filtros en el cuerpo de la solicitud.
     * @param idEquipo
     * @param estado
     * @param urgencia
     * @param userRol Rol del usuario que hace la petición (desde header X-User-Rol)
     * @return ResponseEntity<Map<String, Object>> con los reportes de falla encontrados
     */
    @GetMapping("/reportes-falla")
    public ResponseEntity<Map<String, Object>> consultarReportesFalla(@RequestParam(required = false) Long idEquipo,
                                                                        @RequestParam(required = false) String estado,
                                                                        @RequestParam(required = false) String urgencia,
                                                                        @RequestHeader(value = "X-User-Rol", required = false) String userRol)
    {
        try {
            List<Equipo> equipos = equipoService.consultarReportesFalla(idEquipo, estado, urgencia, userRol);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);

            if (equipos.isEmpty()) {
                response.put("message", "No se encontraron reportes de falla con los criterios especificados");
            } else {
                response.put("message", "Consulta exitosa");
            }

            response.put("count", equipos.size());
            response.put("data", equipos);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}
