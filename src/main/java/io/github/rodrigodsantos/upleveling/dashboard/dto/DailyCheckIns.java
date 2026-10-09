package io.github.rodrigodsantos.upleveling.dashboard.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Um dia do histórico agrupado: o XP total do dia (bônus incluído) e cada hábito com os seus check-ins somados.
 * Os hábitos vêm na ordem do check-in mais recente de cada um.
 */
public record DailyCheckIns(LocalDate date, long xp, List<HabitCheckIns> habits) {
}
