package io.github.rodrigodsantos.upleveling.gamification.dto;

import java.time.LocalDate;

/** Body opcional do check-in: sem date, vale hoje. */
public record CheckInRequest(LocalDate date) {
}
