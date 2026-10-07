package io.github.rodrigodsantos.upleveling;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Nos testes, quem pede um Clock recebe o MutableClock (@Primary vence o Clock do ClockConfig).
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestClockConfiguration {

    /** Quarta-feira: a data "de hoje" padrão de todos os testes. */
    public static final LocalDate TODAY = LocalDate.of(2026, 10, 7);

    @Bean
    @Primary
    MutableClock testClock() {
        return new MutableClock(ZoneId.of("America/Sao_Paulo"), TODAY);
    }
}
