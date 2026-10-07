package io.github.rodrigodsantos.upleveling.dashboard.dto;

import io.github.rodrigodsantos.upleveling.gamification.dto.ProgressResponse;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/** A tela principal do app numa chamada só: os hábitos do dia, o placar do dia e a barra de nível. */
public record TodayResponse(LocalDate date, DayOfWeek dayOfWeek, List<TodayHabit> habits, int completedCount,
                            int totalCount, long xpEarnedToday, ProgressResponse progress) {
}
