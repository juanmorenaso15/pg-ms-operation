package com.pulse_gym.ms_operation.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuración de caché para el manejo de intentos biométricos y otros usos.
 * Utiliza Caffeine para almacenar en memoria con expiración por tiempo.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Crea un CacheManager con una caché llamada "biometricAttempts"
     * que expira después de 15 minutos de inactividad.
     *
     * @return CacheManager configurado
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("biometricAttempts");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .maximumSize(1000));
        return cacheManager;
    }
}