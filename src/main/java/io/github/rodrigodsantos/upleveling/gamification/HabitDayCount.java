package io.github.rodrigodsantos.upleveling.gamification;

import java.time.LocalDate;

/** Uma linha do GROUP BY: quantos check-ins um hábito teve num dia. Montada direto pela consulta (sem entidade). */
public record HabitDayCount(Long habitId, LocalDate date, Long count) {
}
