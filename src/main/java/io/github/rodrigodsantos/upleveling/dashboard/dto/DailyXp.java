package io.github.rodrigodsantos.upleveling.dashboard.dto;

import java.time.LocalDate;

/** Um ponto do gráfico de XP: quanto o usuário ganhou num dia (0 se não fez nada). */
public record DailyXp(LocalDate date, Long xp) {
}
