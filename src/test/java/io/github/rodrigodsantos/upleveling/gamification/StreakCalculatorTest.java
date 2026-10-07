package io.github.rodrigodsantos.upleveling.gamification;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de unidade da regra do streak. "Hoje" é quarta, 07/10/2026; seg = 05/10, sex = 02/10 e 09/10.
 */
class StreakCalculatorTest {

    private static final LocalDate MON_28_09 = LocalDate.of(2026, 9, 28);
    private static final LocalDate WED_30_09 = LocalDate.of(2026, 9, 30);
    private static final LocalDate FRI_02_10 = LocalDate.of(2026, 10, 2);
    private static final LocalDate MON_05_10 = LocalDate.of(2026, 10, 5);
    private static final LocalDate TUE_06_10 = LocalDate.of(2026, 10, 6);
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 7);

    private static final Set<DayOfWeek> EVERY_DAY = Set.of();
    private static final Set<DayOfWeek> MON_WED_FRI = Set.of(MONDAY, WEDNESDAY, FRIDAY);

    @Test
    void noCompletedDaysMeansZero() {
        assertThat(StreakCalculator.currentStreak(Set.of(), EVERY_DAY, TODAY)).isZero();
    }

    @Test
    void countsConsecutiveDaysEndingToday() {
        assertThat(StreakCalculator.currentStreak(Set.of(MON_05_10, TUE_06_10, TODAY), EVERY_DAY, TODAY)).isEqualTo(3);
    }

    @Test
    void todayStillOpenDoesNotBreakTheStreak() {
        assertThat(StreakCalculator.currentStreak(Set.of(MON_05_10, TUE_06_10), EVERY_DAY, TODAY)).isEqualTo(2);
    }

    @Test
    void aMissedDayBreaksTheStreak() {
        // Domingo 04/10 em branco: a sequência que conta é só seg + ter
        Set<LocalDate> completed = Set.of(LocalDate.of(2026, 10, 3), MON_05_10, TUE_06_10);
        assertThat(StreakCalculator.currentStreak(completed, EVERY_DAY, TODAY)).isEqualTo(2);
    }

    @Test
    void daysOutsideTheScheduleAreSkipped() {
        // Seg/qua/sex: as terças e quintas no meio não quebram a sequência
        Set<LocalDate> completed = Set.of(MON_28_09, WED_30_09, FRI_02_10, MON_05_10, TODAY);
        assertThat(StreakCalculator.currentStreak(completed, MON_WED_FRI, TODAY)).isEqualTo(5);
    }

    @Test
    void aMissedScheduledDayBreaksEvenWithUnscheduledDaysAround() {
        // Faltou a sexta 02/10, que estava na agenda
        Set<LocalDate> completed = Set.of(MON_28_09, WED_30_09, MON_05_10, TODAY);
        assertThat(StreakCalculator.currentStreak(completed, MON_WED_FRI, TODAY)).isEqualTo(2);
    }
}
