package com.pulse_gym.ms_operation.services;

import com.pulse_gym.lb_common.client.SocioMembresiaClient;
import com.pulse_gym.lb_common.client.UsuarioClient;
import com.pulse_gym.lb_common.dto.AsistenciaResponseDTO;
import com.pulse_gym.lb_common.dto.EstadoMembresiaResponseDTO;
import com.pulse_gym.lb_common.dto.MessegeGlobalDTO;
import com.pulse_gym.lb_common.dto.RegistroAsistenciaBiometricaDTO;
import com.pulse_gym.lb_common.dto.RegistroAsistenciaDTO;
import com.pulse_gym.lb_common.dto.UsuarioPerfilResponseDTO;
import com.pulse_gym.lb_common.entity.operation.Asistencia;
import com.pulse_gym.lb_common.entity.operation.AuditoriaBiometrica;
import com.pulse_gym.lb_common.entity.operation.Sede;
import com.pulse_gym.lb_common.enums.EnumEstadoAcceso;
import com.pulse_gym.lb_common.enums.EnumTipoAcceso;
import com.pulse_gym.lb_common.services.BiometricJwtService;
import com.pulse_gym.lb_common.services.ValidacionDeRoles;
import com.pulse_gym.ms_operation.repository.AsistenciaRepository;
import com.pulse_gym.ms_operation.repository.AuditoriaBiometricaRepository;
import com.pulse_gym.ms_operation.repository.SedeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
/**
 * Servicio encargado de gestionar el registro y consulta de asistencias del
 * sistema.
 * Incluye lógica para accesos normales, accesos biométricos, validaciones de
 * roles
 * y auditoría de intentos de entrada.
 */
public class AsistenciaService {

    /**
     * Repositorio para persistir y consultar registros de asistencia.
     */
    private final AsistenciaRepository asistenciaRepository;

    /**
     * Repositorio para consultar información y datos de las sedes asociadas.
     */
    private final SedeRepository sedeRepository;

    /**
     * Cliente encargado de obtener los datos del perfil de usuario desde el
     * servicio de usuarios.
     */
    private final UsuarioClient usuarioClient;

    /**
     * Cliente utilizado para consultar el estado de membresía del socio desde el
     * servicio correspondiente.
     */
    private final SocioMembresiaClient socioMembresiaClient;

    /**
     * Servicio responsable de validar, decodificar y comparar tokens biométricos.
     */
    private final BiometricJwtService biometricJwtService;

    /**
     * Repositorio para registrar eventos y resultados de auditoría biométrica.
     */
    private final AuditoriaBiometricaRepository auditoriaRepository;

    /**
     * Mapa en memoria para controlar intentos fallidos de acceso biométrico.
     * La llave corresponde al identificador del usuario y el valor al contador de
     * fallos consecutivos.
     * Se reinicia cuando el usuario realiza un intento exitoso.
     */
    private final ConcurrentHashMap<Long, Integer> intentosFallidos = new ConcurrentHashMap<>();

    /**
     * Registra una nueva entrada de asistencia para un usuario mediante un acceso
     * tradicional.
     *
     * @param request Datos de la solicitud de asistencia, incluyendo usuario, sede
     *                y tipo de acceso.
     * @param userRol Rol del usuario que realiza la petición.
     * @return Mensaje global con el resultado del registro de acceso.
     */
    @Transactional
    public MessegeGlobalDTO registrarEntrada(RegistroAsistenciaDTO request, String userRol) {

        ValidacionDeRoles.validarSocio(userRol);

        Sede sede = sedeRepository.findById(request.getIdSede())
                .orElseThrow(() -> new RuntimeException("Sede no encontrada con ID: " + request.getIdSede()));

        EnumTipoAcceso tipoAcceso;
        try {
            tipoAcceso = EnumTipoAcceso.valueOf(request.getTipoAcceso().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Tipo de acceso no válido. Debe ser WEB o APP");
        }

        UsuarioPerfilResponseDTO usuario = usuarioClient.obtenerUsuarioPorId(request.getIdUsuario());

        if (usuario == null) {
            return registrarAccesoDenegado(request, sede, tipoAcceso,
                    "Usuario no encontrado con ID: " + request.getIdUsuario());
        }

        Asistencia asistencia = new Asistencia();
        asistencia.setIdUsuario(request.getIdUsuario());
        asistencia.setSede(sede);
        asistencia.setFechaHoraEntrada(LocalDateTime.now());
        asistencia.setTipoAcceso(tipoAcceso);
        asistencia.setEstadoAcceso(EnumEstadoAcceso.PERMITIDO);
        asistencia.setMotivoDenegacion(null);

        asistenciaRepository.save(asistencia);

        String nombreCompleto = (usuario.getNombre() != null ? usuario.getNombre() : "") +
                " " + (usuario.getApellido() != null ? usuario.getApellido() : "");
        nombreCompleto = nombreCompleto.trim().isEmpty() ? "Socio" : nombreCompleto;

        return new MessegeGlobalDTO(String.format(
                "Acceso permitido. Bienvenido %s, registro exitoso en sede: %s",
                nombreCompleto,
                sede.getNombreSede()));
    }

    /**
     * Obtiene el historial de asistencias de un usuario específico.
     *
     * @param idUsuario Identificador del usuario cuyo historial se desea consultar.
     * @param userRol   Rol del usuario que solicita la información.
     * @return Lista de registros de asistencia del usuario ordenados por fecha
     *         descendente.
     */
    public List<AsistenciaResponseDTO> consultarHistorialUsuario(Long idUsuario, String userRol) {
        ValidacionDeRoles.validarCualquierRol(userRol);
        List<Asistencia> asistencias = asistenciaRepository.findByIdUsuarioOrderByFechaHoraEntradaDesc(idUsuario);
        if (asistencias.isEmpty()) {
            throw new RuntimeException("El usuario " + idUsuario + " no tiene registros de asistencia");
        }
        return asistencias.stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las asistencias registradas para una sede específica.
     *
     * @param idSede  Identificador de la sede a consultar.
     * @param userRol Rol del usuario que realiza la consulta.
     * @return Lista de asistencias registradas para la sede indicada.
     */
    public List<AsistenciaResponseDTO> consultarAsistenciasPorSede(Long idSede, String userRol) {
        ValidacionDeRoles.validarAdminOEntrenadorORecepcionista(userRol);
        Sede sede = sedeRepository.findById(idSede)
                .orElseThrow(() -> new RuntimeException("Sede no encontrada con ID: " + idSede));
        List<Asistencia> asistencias = asistenciaRepository.findBySedeIdSedeOrderByFechaHoraEntradaDesc(idSede);
        if (asistencias.isEmpty()) {
            throw new RuntimeException("No hay asistencias registradas para la sede: " + sede.getNombreSede());
        }
        return asistencias.stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las asistencias registradas durante el día actual.
     *
     * @param userRol Rol del usuario que realiza la consulta.
     * @return Lista de asistencias del día vigente.
     */
    public List<AsistenciaResponseDTO> consultarAsistenciasDelDia(String userRol) {
        ValidacionDeRoles.validarAdminOEntrenadorORecepcionista(userRol);
        LocalDateTime inicio = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime fin = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        List<Asistencia> asistencias = asistenciaRepository.findByFechaHoraEntradaBetween(inicio, fin);
        return asistencias.stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Registra un acceso denegado en la base de datos y lanza una excepción para
     * abortar el flujo.
     *
     * @param request    Datos del intento de acceso que fue denegado.
     * @param sede       Sede asociada al acceso denegado.
     * @param tipoAcceso Tipo de acceso que se intentó registrar.
     * @param motivo     Razón por la cual se rechazó el acceso.
     * @return Nunca retorna normalmente; lanza una excepción.
     */
    private MessegeGlobalDTO registrarAccesoDenegado(RegistroAsistenciaDTO request, Sede sede,
            EnumTipoAcceso tipoAcceso, String motivo) {
        Asistencia asistencia = new Asistencia();
        asistencia.setIdUsuario(request.getIdUsuario());
        asistencia.setSede(sede);
        asistencia.setFechaHoraEntrada(LocalDateTime.now());
        asistencia.setTipoAcceso(tipoAcceso);
        asistencia.setEstadoAcceso(EnumEstadoAcceso.DENEGADO);
        asistencia.setMotivoDenegacion(motivo);
        asistenciaRepository.save(asistencia);
        throw new RuntimeException("Acceso denegado: " + motivo);
    }

    /**
     * Convierte una entidad de asistencia a su representación para respuestas
     * externas.
     *
     * @param asistencia Entidad a convertir.
     * @return DTO listo para exponer en la capa de servicio o controlador.
     */
    private AsistenciaResponseDTO convertirAResponseDTO(Asistencia asistencia) {
        AsistenciaResponseDTO dto = new AsistenciaResponseDTO();
        dto.setIdAsistencia(asistencia.getIdAsistencia());
        dto.setIdUsuario(asistencia.getIdUsuario());
        dto.setNombreSede(asistencia.getSede() != null ? asistencia.getSede().getNombreSede() : "Sede no registrada");
        dto.setFechaHoraEntrada(asistencia.getFechaHoraEntrada());
        dto.setTipoAcceso(asistencia.getTipoAcceso().name());
        dto.setEstadoAcceso(asistencia.getEstadoAcceso().name());
        dto.setMotivoDenegacion(asistencia.getMotivoDenegacion());
        return dto;
    }

    /**
     * Registra una entrada de asistencia utilizando un token biométrico.
     * Valida el token, compara hashes, verifica membresía activa, controla intentos
     * fallidos,
     * registra auditoría y genera el acceso correspondiente.
     *
     * @param request Datos del acceso biométrico a validar.
     * @return Respuesta con el resultado del registro de asistencia.
     */
    @Transactional
    public MessegeGlobalDTO registrarEntradaBiometrica(RegistroAsistenciaBiometricaDTO request) {
        log.info("=== [HUELLA] Inicio de intento biométrico para usuario ID: {} ===", request.getIdUsuario());
        if (!biometricJwtService.validateToken(request.getToken())) {
            String mensaje = "Token biométrico inválido";
            log.warn("[HUELLA] {}", mensaje);
            registrarAuditoria(request.getIdUsuario(), null, false, mensaje, null);
            throw new RuntimeException(mensaje);
        }

        if (biometricJwtService.isTokenExpired(request.getToken())) {
            String mensaje = "El token biométrico ha expirado";
            log.warn("[HUELLA] {}", mensaje);
            registrarAuditoria(request.getIdUsuario(), null, false, mensaje, null);
            throw new RuntimeException(mensaje);
        }

        Long userIdFromToken = biometricJwtService.extractUserId(request.getToken());
        String deviceIdFromToken = biometricJwtService.extractDeviceId(request.getToken());

        if (!userIdFromToken.equals(request.getIdUsuario())) {
            String mensaje = "El token no corresponde al usuario";
            log.warn("[HUELLA] {}", mensaje);
            registrarAuditoria(request.getIdUsuario(), null, false, mensaje, null);
            throw new RuntimeException(mensaje);
        }

        UsuarioPerfilResponseDTO usuario = usuarioClient.obtenerUsuarioPorIdInterno(request.getIdUsuario());
        if (usuario == null) {
            String mensaje = "Usuario no encontrado con id: " + request.getIdUsuario();
            log.warn("[HUELLA] {}", mensaje);
            registrarAuditoria(request.getIdUsuario(), null, false, mensaje, null);
            throw new RuntimeException(mensaje);
        }

        String hashGuardado = usuario.getBiometricDeviceId();
        if (hashGuardado == null || hashGuardado.trim().isEmpty()) {
            String mensaje = "El usuario no tiene una huella registrada. Contacte con administración.";
            log.warn("[HUELLA] {}", mensaje);
            registrarAuditoria(request.getIdUsuario(), null, false, mensaje,
                    usuario.getIdSede() != null ? usuario.getIdSede().longValue() : null);
            throw new RuntimeException(mensaje);
        }

        String hashDeviceIdToken = biometricJwtService.generateHash(deviceIdFromToken);
        if (hashDeviceIdToken == null || !hashDeviceIdToken.equals(hashGuardado)) {
            incrementarIntentoFallido(request.getIdUsuario());
            String mensaje = "Huella no reconocida. Intente nuevamente.";
            log.warn("[HUELLA] Intento fallido para usuario ID: {}. Intentos actuales: {}",
                    request.getIdUsuario(), intentosFallidos.getOrDefault(request.getIdUsuario(), 0));
            registrarAuditoria(request.getIdUsuario(), hashGuardado.substring(0, 10), false, mensaje,
                    usuario.getIdSede() != null ? usuario.getIdSede().longValue() : null);

            if (intentosFallidos.getOrDefault(request.getIdUsuario(), 0) >= 3) {
                String mensajeBloqueo = "Huella no reconocida después de 3 intentos. Acceso denegado.";
                log.warn("[HUELLA] {}", mensajeBloqueo);
                throw new RuntimeException("Huella no reconocida");
            }
            throw new RuntimeException(mensaje);
        }

        EstadoMembresiaResponseDTO estadoMembresia;
        try {
            estadoMembresia = socioMembresiaClient.consultarEstadoBiometrico(request.getIdUsuario());
            log.info("[HUELLA] Estado de membresía para usuario {}: {}", request.getIdUsuario(),
                    estadoMembresia.getEstado());
        } catch (Exception e) {
            String mensaje = "Error al validar membresía: " + e.getMessage();
            log.error("[HUELLA] {}", mensaje);
            registrarAuditoria(request.getIdUsuario(), hashGuardado.substring(0, 10), false, mensaje,
                    usuario.getIdSede() != null ? usuario.getIdSede().longValue() : null);
            throw new RuntimeException("Error interno al validar membresía. Contacte con administración.");
        }

        if (!estadoMembresia.isActiva()) {
            String mensaje = "Membresía vencida o inactiva";
            log.warn("[HUELLA] {}", mensaje);
            registrarAuditoria(request.getIdUsuario(), hashGuardado.substring(0, 10), false, mensaje,
                    usuario.getIdSede() != null ? usuario.getIdSede().longValue() : null);
            throw new RuntimeException("Membresía vencida");
        }

        Long idSede = usuario.getIdSede() != null ? usuario.getIdSede().longValue() : null;
        if (idSede == null) {
            String mensaje = "El socio no tiene una sede asignada";
            log.warn("[HUELLA] {}", mensaje);
            registrarAuditoria(request.getIdUsuario(), hashGuardado.substring(0, 10), false, mensaje, null);
            throw new RuntimeException(mensaje);
        }

        resetearIntentos(request.getIdUsuario());

        RegistroAsistenciaDTO registroDTO = new RegistroAsistenciaDTO();
        registroDTO.setIdUsuario(request.getIdUsuario());
        registroDTO.setIdSede(idSede);
        registroDTO.setTipoAcceso("BIOMETRICO");
        registroDTO.setDispositivoId("biometric");

        MessegeGlobalDTO resultado = registrarEntradaInterna(registroDTO);

        registrarAuditoria(request.getIdUsuario(), hashGuardado.substring(0, 10), true,
                "Acceso biométrico exitoso", usuario.getIdSede() != null ? usuario.getIdSede().longValue() : null);

        log.info("[HUELLA] Acceso biométrico exitoso para usuario ID: {}", request.getIdUsuario());
        return resultado;
    }

    /**
     * Registra un evento de auditoría para un intento de acceso biométrico.
     *
     * @param idUsuario  Identificador del usuario involucrado.
     * @param hashHuella Hash o referencia parcial de la huella asociada al intento.
     * @param exitoso    Indica si el intento fue exitoso o no.
     * @param mensaje    Texto descriptivo del resultado del intento.
     * @param idSede     Identificador de la sede relacionada con el evento.
     */
    private void registrarAuditoria(Long idUsuario, String hashHuella, boolean exitoso, String mensaje, Long idSede) {
        AuditoriaBiometrica auditoria = new AuditoriaBiometrica();
        auditoria.setIdUsuario(idUsuario);
        auditoria.setHashHuella(hashHuella != null ? hashHuella : "N/A");
        auditoria.setFechaHora(LocalDateTime.now());
        auditoria.setExitoso(exitoso);
        auditoria.setMensaje(mensaje != null && mensaje.length() > 255 ? mensaje.substring(0, 255) : mensaje);
        auditoria.setIdSede(idSede);
        auditoria.setTipoLog("HUELLA");
        auditoriaRepository.save(auditoria);
    }

    /**
     * Incrementa el contador de intentos fallidos para un usuario.
     *
     * @param userId Identificador del usuario cuyos intentos fallidos se van a
     *               contabilizar.
     */
    private void incrementarIntentoFallido(Long userId) {
        intentosFallidos.compute(userId, (key, val) -> (val == null) ? 1 : val + 1);
    }

    /**
     * Resetea el contador de intentos fallidos para un usuario cuando consigue un
     * acceso válido.
     *
     * @param userId Identificador del usuario al que se le reinicia el contador.
     */
    private void resetearIntentos(Long userId) {
        intentosFallidos.remove(userId);
    }

    /**
     * Registra internamente una asistencia sin aplicar validación de rol, útil para
     * el flujo biométrico.
     *
     * @param request Datos del acceso a registrar internamente.
     * @return Mensaje global con el resultado del registro.
     */
    private MessegeGlobalDTO registrarEntradaInterna(RegistroAsistenciaDTO request) {
        Sede sede = sedeRepository.findById(request.getIdSede())
                .orElseThrow(() -> new RuntimeException("Sede no encontrada con ID: " + request.getIdSede()));

        EnumTipoAcceso tipoAcceso;
        try {
            tipoAcceso = EnumTipoAcceso.valueOf(request.getTipoAcceso().toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("tipo acceso no valido. Debe ser WEB, APP o BIOMETRICO");
        }

        UsuarioPerfilResponseDTO usuario = usuarioClient.obtenerUsuarioPorIdInterno(request.getIdUsuario());
        if (usuario == null) {
            return registrarAccesoDenegado(request, sede, tipoAcceso,
                    "Usuario no encontrado con ID: " + request.getIdUsuario());
        }

        Asistencia asistencia = new Asistencia();
        asistencia.setIdUsuario(request.getIdUsuario());
        asistencia.setSede(sede);
        asistencia.setFechaHoraEntrada(LocalDateTime.now());
        asistencia.setTipoAcceso(tipoAcceso);
        asistencia.setEstadoAcceso(EnumEstadoAcceso.PERMITIDO);
        asistencia.setMotivoDenegacion(null);
        asistencia.setDispositivoId(request.getDispositivoId());

        asistenciaRepository.save(asistencia);

        String nombreCompleto = (usuario.getNombre() != null ? usuario.getNombre() : "") +
                " " + (usuario.getApellido() != null ? usuario.getApellido() : "");
        nombreCompleto = nombreCompleto.trim().isEmpty() ? "Socio" : nombreCompleto;

        return new MessegeGlobalDTO(String.format(
                "Acceso biometrico permitido. Bienvenido %s, registro exitoso en sede: %s",
                nombreCompleto,
                sede.getNombreSede()));
    }
}