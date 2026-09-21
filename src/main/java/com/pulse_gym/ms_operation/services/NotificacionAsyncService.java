package com.pulse_gym.ms_operation.services;

import com.pulse_gym.lb_common.client.NotificacionClient;
import com.pulse_gym.lb_common.dto.EnvioEventoMasivoDTO;
import com.pulse_gym.lb_common.dto.EnvioEventoNotificacionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacionAsyncService {

    /**
     * Cliente para interactuar con el microservicio de notificaciones
     */
    private final NotificacionClient notificacionClient;

    /**
     * Envía un evento de notificacion al microservicio de notificaciones de manera asíncrona
     * @param request DTO que contiene el usuario, evento y variables adicionales
     */
    @Async
    public void enviarNotificacionEvento(EnvioEventoNotificacionDTO request) {
        try {
            notificacionClient.enviarPorEvento(request);
            log.info("Evento de notificacion {} enviado correctamente a pg-ms-notifications", request.getEvento());
        } catch (Exception e) {
            log.error("Error al enviar evento de notificacion a pg-ms-notifications: {}", e.getMessage(), e);
        }
    }

    /**
     * Envía un evento de notificacion masivo (a todos los usuarios con
     * telefono registrado) al microservicio de notificaciones, de manera
     * asincrona.
     * @param request DTO con el evento y las variables adicionales
     */
    @Async
    public void enviarNotificacionEventoMasivo(EnvioEventoMasivoDTO request) {
        try {
            notificacionClient.enviarPorEventoMasivo(request);
            log.info("Evento de notificacion masivo {} enviado correctamente a pg-ms-notifications", request.getEvento());
        } catch (Exception e) {
            log.error("Error al enviar evento de notificacion masivo a pg-ms-notifications: {}", e.getMessage(), e);
        }
    }
}
