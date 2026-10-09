package io.github.rodrigodsantos.upleveling.dashboard.dto;

/**
 * Os check-ins de um hábito num dia, somados: 6 copos de água (+5 cada) viram uma linha com count 6 e xp 30.
 * xp é só o valor base; o bônus de sequência vem separado em bonusXp.
 */
public record HabitCheckIns(Long habitId, String habitName, long count, long xp, long bonusXp) {
}
