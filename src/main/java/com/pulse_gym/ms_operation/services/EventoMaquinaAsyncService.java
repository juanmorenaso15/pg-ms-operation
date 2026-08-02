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

    private final EventoMaquinaClient eventoMaquinaClient;

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