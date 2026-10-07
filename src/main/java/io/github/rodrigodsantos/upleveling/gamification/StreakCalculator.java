package io.github.rodrigodsantos.upleveling.gamification;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

/**
 * Streak = quantos dias da agenda seguidos tiveram a meta cumprida, contando para trás a partir de hoje.
 * <p>
 * Dias fora da agenda não quebram a sequência (seg/qua/sex cumpridos = 3, mesmo com ter e qui no meio).
 * Se hoje ainda não foi cumprido, conta a partir de ontem: o dia ainda está aberto e não quebra nada.
 */
public final class StreakCalculator {

    private StreakCalculator() {
    }

    /**
     * @param completedDates dias em que a meta foi cumprida
     * @param scheduledDays  dias da agenda do hábito (vazio = todos os dias)
     */
    public static int currentStreak(Set<LocalDate> completedDates, Set<DayOfWeek> scheduledDays, LocalDate today) {
        if (completedDates.isEmpty()) {
            return 0;
        }
        LocalDate earliest = Collections.min(completedDates);
        LocalDate day = completedDates.contains(today) ? today : today.minusDays(1);

        int streak = 0;
        while (!day.isBefore(earliest)) {
            boolean scheduled = scheduledDays.isEmpty() || scheduledDays.contains(day.getDayOfWeek());
            if (scheduled) {
                if (!completedDates.contains(day)) {
                    break;
                }
                streak++;
            }
            day = day.minusDays(1);
        }
        return streak;
    }
}
