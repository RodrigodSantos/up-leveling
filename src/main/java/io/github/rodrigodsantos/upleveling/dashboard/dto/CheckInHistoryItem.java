package io.github.rodrigodsantos.upleveling.dashboard.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Um item do histórico. Traz o nome do hábito (mesmo se ele foi excluído depois): o histórico não se apaga. */
public record CheckInHistoryItem(Long id, Long habitId, String habitName, LocalDate date, Integer xp, Integer bonusXp,
                                 LocalDateTime createdAt) {
}
