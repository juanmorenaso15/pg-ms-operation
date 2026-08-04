package com.pulse_gym.ms_operation.services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.pulse_gym.lb_common.client.EventoAccesoClient;
import com.pulse_gym.lb_common.dto.EventoAccesoRequestDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventoAccesoAsyncService {

    /**
     * Inyección del cliente Feign para comunicarse con el microservicio de
     * reportes.
     */
    private final EventoAccesoClient eventoAccesoClient;

    /**
     * Metodo asincrono para el envio del evento al pg-ms-reports
     * Implementado para que no se corte el flujo de donde se aplique
     * @param eventoAccesoRequestDTO
     */
    @Async
    public void enviarEventoAcceso(EventoAccesoRequestDTO eventoAccesoRequestDTO) {
        try {
            log.info("enviando evento de reportes para socio ID: {}", eventoAccesoRequestDTO.getSocioId());
            eventoAccesoClient.enviarEventoAcceso(eventoAccesoRequestDTO);
            log.info("Evento enviado correctamente al pg-ms-reports");
        } catch (Exception e) {
            log.info("Se crea error en el envio DEL EVENTO", e.getMessage(), e);

        }
    }
}
