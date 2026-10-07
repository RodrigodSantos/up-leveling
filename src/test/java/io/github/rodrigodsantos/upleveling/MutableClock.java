package io.github.rodrigodsantos.upleveling;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Relógio que o teste controla: "hoje é quarta, 07/10/2026" ou "avance para amanhã".
 * Substitui o Clock real só nos testes, sem mudar nenhuma linha do código de produção.
 */
public class MutableClock extends Clock {

    private final ZoneId zone;
    private Instant instant;

    public MutableClock(ZoneId zone, LocalDate today) {
        this.zone = zone;
        setToday(today);
    }

    /** Meio-dia no fuso do relógio, longe da virada do dia. */
    public void setToday(LocalDate today) {
        this.instant = today.atTime(12, 0).atZone(zone).toInstant();
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return Clock.fixed(instant, zone);
    }

    @Override
    public Instant instant() {
        return instant;
    }
}
