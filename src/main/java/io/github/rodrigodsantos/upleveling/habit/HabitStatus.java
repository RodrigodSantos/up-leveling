package io.github.rodrigodsantos.upleveling.habit;

public enum HabitStatus {
    ACTIVE,
    PAUSED,
    /** Exclusão lógica: a linha fica no banco para o histórico de check-ins e XP continuar apontando para ela. */
    DELETED
}
