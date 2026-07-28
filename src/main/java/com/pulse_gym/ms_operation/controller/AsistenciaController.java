package com.pulse_gym.ms_operation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pulse_gym.lb_common.dto.AsistenciaResponseDTO;
import com.pulse_gym.lb_common.dto.MessegeGlobalDTO;
import com.pulse_gym.lb_common.dto.RegistroAsistenciaBiometricaDTO;
import com.pulse_gym.lb_common.dto.RegistroAsistenciaDTO;
import com.pulse_gym.ms_operation.services.AsistenciaService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/asistencias")
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    /**
     * Registrar entrada de socio (desde WEB o APP)
     * POST /api/asistencias/entrada
     * 
     * Se valida que la peticion solo la puede hacer un socio
     * 
     * @param request Datos de la asistencia a registrar
     * @param userRol Rol del usuario que hace la petición (desde header X-User-Rol)
     * @return ResponseEntity con el resultado de la operación
     */
    @PostMapping("/entrada")
    public ResponseEntity<MessegeGlobalDTO> registrarEntrada(
            @Valid @RequestBody RegistroAsistenciaDTO request,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {

        MessegeGlobalDTO response = asistenciaService.registrarEntrada(request, userRol);
        return ResponseEntity.ok(response);
    }

    /**
     * Consultar historial de asistencias de un socio.
     * 
     * @param idUsuario ID del socio
     * @param userRol   Rol del socio
     * @return ResponseEntity con el historial de asistencias
     */
    @GetMapping("/historial/usuario/{idUsuario}")
    public ResponseEntity<List<AsistenciaResponseDTO>> consultarHistorialUsuario(
            @PathVariable Long idUsuario,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {

        List<AsistenciaResponseDTO> historial = asistenciaService.consultarHistorialUsuario(idUsuario, userRol);
        return ResponseEntity.ok(historial);
    }

    /**
     * Consultar asistencias por sede.
     * 
     * @param idSede  ID de la sede
     * @param userRol Rol del socio
     * @return ResponseEntity con las asistencias encontradas
     */
    @GetMapping("/sede/{idSede}")
    public ResponseEntity<List<AsistenciaResponseDTO>> consultarAsistenciasPorSede(
            @PathVariable Long idSede,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {

        List<AsistenciaResponseDTO> asistencias = asistenciaService.consultarAsistenciasPorSede(idSede, userRol);
        return ResponseEntity.ok(asistencias);
    }

    /**
     * Consultar asistencias del día actual.
     * 
     * @param userRol Rol del usuario
     * @return ResponseEntity con las asistencias registradas hoy
     */
    @GetMapping("/hoy")
    public ResponseEntity<List<AsistenciaResponseDTO>> consultarAsistenciasDelDia(
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {

        List<AsistenciaResponseDTO> asistencias = asistenciaService.consultarAsistenciasDelDia(userRol);
        return ResponseEntity.ok(asistencias);
    }

    /**
     * Registrar entrada con huella digital (biométrica).
     * POST /api/asistencias/entrada-biometrica
     * Ruta pública (no requiere JWT normal) - validada por el gateway.
     * 
     * @param request Datos del registro biométrico
     * @return ResponseEntity con el resultado
     */
    @PostMapping("/entrada-biometrica")
    public ResponseEntity<MessegeGlobalDTO> registrarEntradaBiometrica(
            @Valid @RequestBody RegistroAsistenciaBiometricaDTO request) {

        MessegeGlobalDTO response = asistenciaService.registrarEntradaBiometrica(request);
        return ResponseEntity.ok(response);
    }
}