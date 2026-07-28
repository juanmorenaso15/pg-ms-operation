// SedeController.java
package com.pulse_gym.ms_operation.controller;

import com.pulse_gym.lb_common.dto.MessegeGlobalDTO;
import com.pulse_gym.lb_common.dto.SedeRequestDTO;
import com.pulse_gym.lb_common.dto.SedeResponseDTO;
import com.pulse_gym.lb_common.dto.SedeUpdateDTO;
import com.pulse_gym.ms_operation.services.SedeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sedes")
@RequiredArgsConstructor
public class SedeController {

    /**
     * Inyeccion de SedeService para manejar la lógica de negocio relacionada con
     * las sedes, como el registro y la obtención de sedes.
     */
    private final SedeService sedeService;

    /**
     * Endpoint para registrar una nueva sede. Recibe un objeto SedeRequestDTO en
     * el cuerpo de la solicitud,
     * 
     * @param request
     * @param userRol Rol del usuario que hace la petición (desde header X-User-Rol)
     * @return ResponseEntity<Map<String, Object>> con la respuesta de la creación
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> crearSede(@Valid @RequestBody SedeRequestDTO request, 
                                                        @RequestHeader(value = "X-User-Rol", required = false) String userRol) {
        try {
            MessegeGlobalDTO response = sedeService.crearSede(request, userRol);
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", response.getMessage());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Endpoint para obtener todas las sedes registradas en la base de datos
     * GET /api/sedes
     * @return ResponseEntity<Map<String, Object>> con la respuesta de la consulta
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerTodasLasSedes(@RequestHeader(value = "X-User-Rol", required = false) String userRol) {
        try {
            List<SedeResponseDTO> sedes = sedeService.obtenerTodasLasSedes(userRol);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Consulta exitosa");
            response.put("count", sedes.size());
            response.put("data", sedes);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Endpoint para obtener una sede por su ID
     * GET /api/sedes/{id}
     * @param id
     * @return ResponseEntity<Map<String, Object>> con la respuesta de la consulta
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerSedePorId(@PathVariable Long id) {
        try {
            SedeResponseDTO sede = sedeService.obtenerSedePorId(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Sede encontrada");
            response.put("data", sede);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Endpoint para actualizar una sede
     * PUT /api/sedes/{id}
     * 
     * @param id
     * @param request
     * @param userRol Rol del usuario que hace la petición (desde header X-User-Rol)
     * @return ResponseEntity<Map<String, Object>> con la respuesta de la actualización
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> actualizarSede(@PathVariable Long id,
                                                            @Valid @RequestBody SedeUpdateDTO request,
                                                            @RequestHeader(value = "X-User-Rol", required = false) String userRol) 
    {
        try {
            MessegeGlobalDTO response = sedeService.actualizarSede(id, request, userRol);
            
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
     * Endpoint para eliminar una sede por su ID
     * DELETE /api/sedes/{id}
     * @param id
     * @param userRol Rol del usuario que hace la petición (desde header X-User-Rol)
     * @return ResponseEntity<Map<String, Object>> con la respuesta de la eliminación
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminarSede(@PathVariable Long id, 
                                                            @RequestHeader(value = "X-User-Rol", required = false) String userRol)
    {
        try {
            MessegeGlobalDTO response = sedeService.eliminarSede(id, userRol);
            
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
     * Endpoint para buscar sedes por su nombre (por completo)
     * GET /api/sedes/buscar/nombre
     * @param nombre
     * @param userRol Rol del usuario que hace la petición (desde header X-User-Rol)
     * @return ResponseEntity<Map<String, Object>> con la respuesta de la consulta
     */
    @GetMapping("/buscar/nombre")
    public ResponseEntity<Map<String, Object>> buscarSedesPorNombre(@RequestParam String nombre, 
                                                                    @RequestHeader(value = "X-User-Rol", required = false) String userRol
    ) {
        try {
            List<SedeResponseDTO> sedes = sedeService.buscarSedesPorNombre(nombre, userRol);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Sedes encontradas");
            response.put("count", sedes.size());
            response.put("data", sedes);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Endpoint para buscar sedes por su ciudad 
     * @param ciudad
     * @param userRol Rol del usuario que hace la petición (desde header X-User-Rol)
     * @return ResponseEntity<Map<String, Object>> con la respuesta de la consulta
     */
    @GetMapping("/buscar/ciudad")
    public ResponseEntity<Map<String, Object>> buscarSedesPorCiudad(@RequestParam String ciudad,
                                                                    @RequestHeader(value = "X-User-Rol", required = false) String userRol) {
        try {
            List<SedeResponseDTO> sedes = sedeService.buscarSedesPorCiudad(ciudad, userRol);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Sedes encontradas");
            response.put("count", sedes.size());
            response.put("data", sedes);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

}