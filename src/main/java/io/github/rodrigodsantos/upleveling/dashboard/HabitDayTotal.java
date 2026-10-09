package io.github.rodrigodsantos.upleveling.dashboard;

import io.github.rodrigodsantos.upleveling.dashboard.dto.HabitCheckIns;

import java.time.LocalDate;

/**
 * Linha da consulta agrupada (dia + hábito). Não sai na API: o service junta as linhas de cada dia em um DailyCheckIns.
 * Os números chegam como Long porque é o tipo que o COUNT e o SUM do JPQL devolvem.
 */
public record HabitDayTotal(LocalDate date, Long habitId, String habitName, Long count, Long xp, Long bonusXp) {

    public HabitCheckIns toHabitCheckIns() {
        return new HabitCheckIns(habitId, habitName, count, xp, bonusXp);
    }

    public long totalXp() {
        return xp + bonusXp;
    }
}
