// ProveedorController.java
package com.pulse_gym.ms_operation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pulse_gym.lb_common.dto.MessegeGlobalDTO;
import com.pulse_gym.lb_common.dto.ProveedorRequestDTO;
import com.pulse_gym.lb_common.dto.ProveedorResponseDTO;
import com.pulse_gym.ms_operation.services.ProveedorService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    /**
     * Inyeccion de ProveedorService para manejar la lógica de negocio relacionada con
     * los proveedores, como el registro y la obtención de proveedores.
     */
    private final ProveedorService proveedorService;

    /**
     * Endpoint para registrar un nuevo proveedor. Recibe un objeto ProveedorRequestDTO en
     * el cuerpo de la solicitud, y registra el proveedor en la base de datos.
     * @param request
     * @param userRol Rol del usuario que hace la petición (desde header X-User-Rol)
     * @return ResponseEntity<Map<String, Object>> con el resultado del registro
     */
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, Object>> registrarProveedor(@Valid @RequestBody ProveedorRequestDTO request, 
                                                                  @RequestHeader(value = "X-User-Rol", required = false) String userRol
    ) {
        try {
            MessegeGlobalDTO response = proveedorService.registrarProveedor(request, userRol);
            
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
     * Endpoint para consultar todos los proveedores registrados en la base de datos.
     * @param userRol Rol del usuario que hace la petición (desde header X-User-Rol)
     * @return ResponseEntity<Map<String, Object>> con los proveedores registrados
     */
    @GetMapping("/todos")
    public ResponseEntity<Map<String, Object>> consultarTodosProveedores(@RequestHeader(value = "X-User-Rol", required = false) String userRol) {
        try {
            List<ProveedorResponseDTO> proveedores = proveedorService.consultarTodosProveedores(userRol);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Consulta exitosa");
            response.put("count", proveedores.size());
            response.put("data", proveedores);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Endpoint para consultar un proveedor por su ID.
     * @param id
     * @return ResponseEntity<Map<String, Object>> con el proveedor encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> consultarProveedorPorId(@PathVariable Long id,
                                                                        @RequestHeader(value = "X-User-Rol", required = false) String userRol
    ) {
        try {
            ProveedorResponseDTO proveedor = proveedorService.consultarProveedorPorId(id, userRol);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Proveedor encontrado");
            response.put("data", proveedor);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Endpoint para buscar proveedores por su nombre.
     * @param nombre+
     * @param userRol Rol del usuario que hace la petición (desde header X-User-Rol)
     * @return ResponseEntity<Map<String, Object>> con los proveedores encontrados
     */
    @GetMapping("/buscar")
    public ResponseEntity<Map<String, Object>> buscarProveedoresPorNombre(@RequestParam String nombre, 
                                                                          @RequestHeader(value = "X-User-Rol", required = false) String userRol
    ) {
        try { 
            List<ProveedorResponseDTO> proveedores = proveedorService.buscarProveedoresPorNombre(nombre, userRol);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Consulta exitosa");
            response.put("count", proveedores.size());
            response.put("data", proveedores);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
}