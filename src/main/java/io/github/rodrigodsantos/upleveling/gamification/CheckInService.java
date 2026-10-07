package io.github.rodrigodsantos.upleveling.gamification;

import io.github.rodrigodsantos.upleveling.gamification.dto.CheckInResponse;
import io.github.rodrigodsantos.upleveling.gamification.dto.ProgressResponse;
import io.github.rodrigodsantos.upleveling.habit.Habit;
import io.github.rodrigodsantos.upleveling.habit.HabitRepository;
import io.github.rodrigodsantos.upleveling.shared.exception.BusinessRuleException;
import io.github.rodrigodsantos.upleveling.shared.exception.ResourceNotFoundException;
import io.github.rodrigodsantos.upleveling.shared.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static io.github.rodrigodsantos.upleveling.habit.HabitStatus.DELETED;
import static io.github.rodrigodsantos.upleveling.habit.HabitStatus.PAUSED;

@Service
public class CheckInService {

    /** A cada quantos dias seguidos vem o bônus. */
    static final int BONUS_EVERY_DAYS = 7;

    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final DateTimeFormatter DATE_PT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final HabitRepository habitRepository;
    private final CheckInRepository checkInRepository;
    private final CurrentUser currentUser;
    private final Clock clock;

    public CheckInService(HabitRepository habitRepository, CheckInRepository checkInRepository,
                          CurrentUser currentUser, Clock clock) {
        this.habitRepository = habitRepository;
        this.checkInRepository = checkInRepository;
        this.currentUser = currentUser;
        this.clock = clock;
    }

    @Transactional
    public CheckInResponse checkIn(Long habitId, LocalDate requestedDate) {
        Habit habit = findOwn(habitId);
        if (habit.getStatus() == PAUSED) {
            throw new BusinessRuleException("O hábito '" + habit.getName() + "' está pausado. Reative-o para fazer check-in");
        }
        LocalDate today = LocalDate.now(clock);
        LocalDate date = CheckInDates.resolve(requestedDate, today);
        if (!habit.isScheduledOn(date.getDayOfWeek())) {
            throw new BusinessRuleException("O hábito '" + habit.getName() + "' não está agendado para "
                    + date.getDayOfWeek().getDisplayName(TextStyle.FULL, PT_BR));
        }

        int count = (int) checkInRepository.countByHabitIdAndCheckInDate(habit.getId(), date);
        if (count >= habit.getDailyTarget()) {
            throw new BusinessRuleException("Meta do dia já atingida (" + count + "/" + habit.getDailyTarget() + ")");
        }

        long xpBefore = checkInRepository.totalXp(habit.getUserId());
        int bonus = completesDay(habit, count) ? streakBonus(habit, date) : 0;
        checkInRepository.save(new CheckIn(habit.getId(), habit.getUserId(), date, habit.getXpReward(), bonus));

        return response(habit, date, today, habit.getXpReward() + bonus, bonus, xpBefore);
    }

    /** Remove o último check-in do dia; o XP (bônus incluído) volta junto. */
    @Transactional
    public CheckInResponse undo(Long habitId, LocalDate requestedDate) {
        Habit habit = findOwn(habitId);
        LocalDate today = LocalDate.now(clock);
        LocalDate date = CheckInDates.resolve(requestedDate, today);

        CheckIn last = checkInRepository.findFirstByHabitIdAndCheckInDateOrderByIdDesc(habit.getId(), date)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nenhum check-in de '" + habit.getName() + "' em " + date.format(DATE_PT)));

        long xpBefore = checkInRepository.totalXp(habit.getUserId());
        checkInRepository.delete(last);

        return response(habit, date, today, -last.totalXp(), -last.getBonusXp(), xpBefore);
    }

    @Transactional(readOnly = true)
    public ProgressResponse progress() {
        return ProgressResponse.of(checkInRepository.totalXp(currentUser.id()));
    }


    private boolean completesDay(Habit habit, int countBefore) {
        return countBefore + 1 == habit.getDailyTarget();
    }

    /** Se este check-in fecha um 7º, 14º, 21º... dia seguido: +50% do XP do dia do hábito. */
    private int streakBonus(Habit habit, LocalDate date) {
        Set<LocalDate> completed = completedDates(habit, date);
        completed.add(date); // o dia que este check-in está completando
        int streak = StreakCalculator.currentStreak(completed, habit.getDays(), date);
        return streak % BONUS_EVERY_DAYS == 0 ? habit.getXpReward() * habit.getDailyTarget() / 2 : 0;
    }

    private CheckInResponse response(Habit habit, LocalDate date, LocalDate today, int xpChange, int bonusXp,
                                     long xpBefore) {
        int dayCount = (int) checkInRepository.countByHabitIdAndCheckInDate(habit.getId(), date);
        long xpAfter = checkInRepository.totalXp(habit.getUserId());
        int streak = StreakCalculator.currentStreak(completedDates(habit, today), habit.getDays(), today);
        boolean leveledUp = Levels.levelFor(xpAfter) > Levels.levelFor(xpBefore);

        return new CheckInResponse(habit.getId(), date, xpChange, bonusXp, dayCount, habit.getDailyTarget(),
                dayCount >= habit.getDailyTarget(), streak, leveledUp, ProgressResponse.of(xpAfter));
    }

    private Set<LocalDate> completedDates(Habit habit, LocalDate until) {
        return new HashSet<>(checkInRepository.findCompletedDates(habit.getId(), until, habit.getDailyTarget()));
    }

    /** De outro usuário, inexistente ou excluído: 404, como nos endpoints de hábito. */
    private Habit findOwn(Long habitId) {
        return habitRepository.findByIdAndUserIdAndStatusNot(habitId, currentUser.id(), DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("Hábito", habitId));
    }
}
