package com.pulse_gym.ms_operation.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Servicio encargado de gestionar los intentos fallidos de autenticación biométrica.
 * Almacena en caché el número de intentos fallidos por usuario,
 * y permite reiniciar el contador al éxito o después de un tiempo.
 */
@Service
@Slf4j
public class BiometricAttemptService {

    private static final int MAX_ATTEMPTS = 3;
    private Cache<Long, Integer> attemptCache;

    @PostConstruct
    public void init() {
        attemptCache = Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build();
    }

    /**
     * Obtiene el número de intentos fallidos actuales para un usuario.
     * Si no hay registro, retorna 0.
     *
     * @param userId ID del usuario
     * @return número de intentos fallidos
     */
    public int getFailedAttempts(Long userId) {
        Integer attempts = attemptCache.getIfPresent(userId);
        return attempts != null ? attempts : 0;
    }

    /**
     * Incrementa el contador de intentos fallidos para un usuario.
     *
     * @param userId ID del usuario
     * @return nuevo número de intentos fallidos
     */
    public int incrementFailedAttempts(Long userId) {
        int current = getFailedAttempts(userId);
        int newCount = current + 1;
        attemptCache.put(userId, newCount);
        log.info("Intentos fallidos para usuario {}: {}", userId, newCount);
        return newCount;
    }

    /**
     * Reinicia el contador de intentos fallidos (se llama al éxito o al superar el límite
     * y se muestra el mensaje).
     *
     * @param userId ID del usuario
     */
    public void resetAttempts(Long userId) {
        attemptCache.invalidate(userId);
        log.info("Reiniciando intentos fallidos para usuario {}", userId);
    }

    /**
     * Verifica si el usuario ha superado el límite de intentos fallidos.
     *
     * @param userId ID del usuario
     * @return true si ha superado el límite, false en caso contrario
     */
    public boolean isBlocked(Long userId) {
        return getFailedAttempts(userId) >= MAX_ATTEMPTS;
    }
}