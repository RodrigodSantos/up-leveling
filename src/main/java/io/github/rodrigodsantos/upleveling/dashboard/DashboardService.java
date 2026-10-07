package io.github.rodrigodsantos.upleveling.dashboard;

import io.github.rodrigodsantos.upleveling.dashboard.dto.CheckInHistoryItem;
import io.github.rodrigodsantos.upleveling.dashboard.dto.DailyXp;
import io.github.rodrigodsantos.upleveling.dashboard.dto.TodayHabit;
import io.github.rodrigodsantos.upleveling.dashboard.dto.TodayResponse;
import io.github.rodrigodsantos.upleveling.gamification.CheckInDates;
import io.github.rodrigodsantos.upleveling.gamification.CheckInRepository;
import io.github.rodrigodsantos.upleveling.gamification.HabitDayCount;
import io.github.rodrigodsantos.upleveling.gamification.StreakCalculator;
import io.github.rodrigodsantos.upleveling.gamification.dto.ProgressResponse;
import io.github.rodrigodsantos.upleveling.habit.Habit;
import io.github.rodrigodsantos.upleveling.habit.HabitRepository;
import io.github.rodrigodsantos.upleveling.shared.exception.InvalidRequestException;
import io.github.rodrigodsantos.upleveling.shared.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.rodrigodsantos.upleveling.habit.HabitStatus.ACTIVE;

/**
 * Leituras prontas para as telas do frontend: só consulta, nunca altera nada.
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    static final int DEFAULT_XP_HISTORY_DAYS = 30;
    static final int MAX_RANGE_DAYS = 366;
    static final int MAX_PAGE_SIZE = 100;

    private final HabitRepository habitRepository;
    private final CheckInRepository checkInRepository;
    private final CurrentUser currentUser;
    private final Clock clock;

    public DashboardService(HabitRepository habitRepository, CheckInRepository checkInRepository,
                            CurrentUser currentUser, Clock clock) {
        this.habitRepository = habitRepository;
        this.checkInRepository = checkInRepository;
        this.currentUser = currentUser;
        this.clock = clock;
    }

    /** Hábitos ativos agendados para o dia, com o andamento de cada um. */
    public TodayResponse today(LocalDate requestedDate) {
        Long userId = currentUser.id();
        LocalDate date = CheckInDates.resolve(requestedDate, LocalDate.now(clock));

        // Uma consulta só para todos os hábitos: habitId → (dia → check-ins)
        Map<Long, Map<LocalDate, Long>> countsByHabit = checkInRepository.findDailyCounts(userId, date).stream()
                .collect(Collectors.groupingBy(HabitDayCount::habitId,
                        Collectors.toMap(HabitDayCount::date, HabitDayCount::count)));

        List<TodayHabit> habits = habitRepository.findByUserIdAndStatusInOrderByNameAsc(userId, List.of(ACTIVE)).stream()
                .filter(habit -> habit.isScheduledOn(date.getDayOfWeek()))
                .map(habit -> toTodayHabit(habit, countsByHabit.getOrDefault(habit.getId(), Map.of()), date))
                .toList();

        int completed = (int) habits.stream().filter(TodayHabit::completed).count();
        return new TodayResponse(date, date.getDayOfWeek(), habits, completed, habits.size(),
                checkInRepository.xpOn(userId, date), ProgressResponse.of(checkInRepository.totalXp(userId)));
    }

    public Page<CheckInHistoryItem> history(Long habitId, LocalDate from, LocalDate to, Pageable pageable) {
        validateRange(from, to);
        // Só página e tamanho: a ordem é fixa na consulta (mais recente primeiro)
        Pageable page = PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), MAX_PAGE_SIZE));
        return checkInRepository.findHistory(currentUser.id(), habitId, from, to, page);
    }

    /** XP por dia, com os dias sem check-in valendo 0 (o gráfico não fica com buracos). */
    public List<DailyXp> xpHistory(LocalDate requestedFrom, LocalDate requestedTo) {
        LocalDate to = requestedTo == null ? LocalDate.now(clock) : requestedTo;
        LocalDate from = requestedFrom == null ? to.minusDays(DEFAULT_XP_HISTORY_DAYS - 1) : requestedFrom;
        validateRange(from, to);

        Map<LocalDate, Long> xpByDay = checkInRepository.findDailyXp(currentUser.id(), from, to).stream()
                .collect(Collectors.toMap(DailyXp::date, DailyXp::xp));

        List<DailyXp> days = new ArrayList<>();
        for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
            days.add(new DailyXp(day, xpByDay.getOrDefault(day, 0L)));
        }
        return days;
    }

    private TodayHabit toTodayHabit(Habit habit, Map<LocalDate, Long> countsByDay, LocalDate date) {
        int count = countsByDay.getOrDefault(date, 0L).intValue();
        Set<LocalDate> completedDates = countsByDay.entrySet().stream()
                .filter(day -> day.getValue() >= habit.getDailyTarget())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        int streak = StreakCalculator.currentStreak(completedDates, habit.getDays(), date);
        return new TodayHabit(habit.getId(), habit.getName(), habit.getXpReward(), habit.getDailyTarget(), count,
                count >= habit.getDailyTarget(), streak);
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            return;
        }
        if (from.isAfter(to)) {
            throw new InvalidRequestException("A data inicial (" + from + ") não pode ser depois da final (" + to + ")");
        }
        if (ChronoUnit.DAYS.between(from, to) + 1 > MAX_RANGE_DAYS) {
            throw new InvalidRequestException("O período pode ter no máximo " + MAX_RANGE_DAYS + " dias");
        }
    }
}
