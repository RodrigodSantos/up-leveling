package io.github.rodrigodsantos.upleveling.dashboard.dto;

/**
 * Um hábito no resumo do dia: o suficiente para desenhar o card com o botão de check-in.
 *
 * @param count     check-ins feitos no dia
 * @param completed se a meta do dia foi atingida (count == dailyTarget)
 * @param streak    sequência atual do hábito
 */
public record TodayHabit(Long id, String name, int xpReward, int dailyTarget, int count, boolean completed,
                         int streak) {
}
