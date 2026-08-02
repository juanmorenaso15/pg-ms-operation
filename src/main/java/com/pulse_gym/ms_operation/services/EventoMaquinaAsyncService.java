package com.pulse_gym.ms_operation.services;

import com.pulse_gym.lb_common.client.EventoMaquinaClient;
import com.pulse_gym.lb_common.dto.EventoMaquinaRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class EventoMaquinaAsyncService {

    /**
     * Cliente para interactuar con el microservicio de reportes
     */
    private final EventoMaquinaClient eventoMaquinaClient;

    /**
     * Envía un evento de máquina al microservicio de reportes de manera asíncrona
     * @param request DTO que contiene la información del evento de máquina
     */
    @Async
    public void enviarEventoMaquina(EventoMaquinaRequestDTO request) {
        try {
            eventoMaquinaClient.enviarEventoMaquina(request);
            log.info("Evento de máquina enviado correctamente a pg-ms-reports");
        } catch (Exception e) {
            log.error("Error al enviar evento de máquina a pg-ms-reports: {}", e.getMessage(), e);
        }
    }
}