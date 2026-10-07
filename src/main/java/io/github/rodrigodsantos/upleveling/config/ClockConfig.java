package io.github.rodrigodsantos.upleveling.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Relógio injetável: o código pergunta "que dia é hoje?" ao Clock, e não direto ao sistema.
 * <p>
 * Dois ganhos: o fuso fica explícito (um servidor em UTC viraria o dia às 21h de Brasília) e os testes
 * trocam este bean por um relógio controlado, para simular "hoje é quarta" ou "passaram 7 dias".
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock(@Value("${upleveling.time-zone}") String timeZone) {
        return Clock.system(ZoneId.of(timeZone));
    }
}
