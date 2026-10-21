package io.github.rodrigodsantos.upleveling.gamification;

/** Uma linha do GROUP BY: o XP total de um usuário. Montada direto pela consulta (sem entidade). */
public record UserXp(Long userId, Long totalXp) {
}
