package io.github.rodrigodsantos.upleveling.gamification.dto;

import java.time.LocalDate;

/**
 * Resultado de um check-in (ou de desfazê-lo), já com tudo que a tela precisa atualizar.
 *
 * @param xpChange     XP ganho (positivo) ou devolvido ao desfazer (negativo), bônus incluído
 * @param bonusXp      parte do xpChange que veio do bônus de streak
 * @param dayCount     check-ins do hábito naquele dia, depois desta operação
 * @param dayCompleted se o dia atingiu a meta (dayCount == dailyTarget)
 * @param streak       sequência atual do hábito, contada a partir de hoje
 * @param leveledUp    se este check-in fez o usuário subir de nível (o frontend mostra a animação)
 */
public record CheckInResponse(Long habitId, LocalDate date, int xpChange, int bonusXp, int dayCount, int dailyTarget,
                              boolean dayCompleted, int streak, boolean leveledUp, ProgressResponse progress) {
}
