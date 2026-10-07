package io.github.rodrigodsantos.upleveling.gamification;

import io.github.rodrigodsantos.upleveling.dashboard.dto.CheckInHistoryItem;
import io.github.rodrigodsantos.upleveling.dashboard.dto.DailyXp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    long countByHabitIdAndCheckInDate(Long habitId, LocalDate checkInDate);

    /** O último do dia: é o que o "desfazer" remove. */
    Optional<CheckIn> findFirstByHabitIdAndCheckInDateOrderByIdDesc(Long habitId, LocalDate checkInDate);

    /** Dias (até a data informada) em que o hábito atingiu a meta: a base do cálculo do streak. */
    @Query("""
            SELECT c.checkInDate FROM CheckIn c
            WHERE c.habitId = :habitId AND c.checkInDate <= :until
            GROUP BY c.checkInDate
            HAVING COUNT(c) >= :dailyTarget
            """)
    List<LocalDate> findCompletedDates(@Param("habitId") Long habitId, @Param("until") LocalDate until,
                                       @Param("dailyTarget") long dailyTarget);

    /**
     * Check-ins por hábito e por dia, de todos os hábitos do usuário, numa consulta só.
     * O resumo do dia calcula o streak de cada hábito a partir daqui, sem uma consulta por hábito (N+1).
     */
    @Query("""
            SELECT new io.github.rodrigodsantos.upleveling.gamification.HabitDayCount(c.habitId, c.checkInDate, COUNT(c))
            FROM CheckIn c
            WHERE c.userId = :userId AND c.checkInDate <= :until
            GROUP BY c.habitId, c.checkInDate
            """)
    List<HabitDayCount> findDailyCounts(@Param("userId") Long userId, @Param("until") LocalDate until);

    /** XP total do usuário = soma dos check-ins. Hábitos excluídos continuam contando: o XP já foi conquistado. */
    @Query("SELECT COALESCE(SUM(c.xp + c.bonusXp), 0) FROM CheckIn c WHERE c.userId = :userId")
    long totalXp(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(c.xp + c.bonusXp), 0) FROM CheckIn c WHERE c.userId = :userId AND c.checkInDate = :date")
    long xpOn(@Param("userId") Long userId, @Param("date") LocalDate date);

    /** Só os dias com check-in; quem preenche os dias vazios com zero é o service. */
    @Query("""
            SELECT new io.github.rodrigodsantos.upleveling.dashboard.dto.DailyXp(c.checkInDate, SUM(c.xp + c.bonusXp))
            FROM CheckIn c
            WHERE c.userId = :userId AND c.checkInDate BETWEEN :from AND :to
            GROUP BY c.checkInDate
            """)
    List<DailyXp> findDailyXp(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    /**
     * Histórico paginado, do mais recente para o mais antigo. Filtro nulo = sem filtro.
     * O JOIN com Habit traz o nome do hábito; a ordenação é fixa (o cliente não manda "sort").
     */
    @Query(value = """
            SELECT new io.github.rodrigodsantos.upleveling.dashboard.dto.CheckInHistoryItem(
                       c.id, c.habitId, h.name, c.checkInDate, c.xp, c.bonusXp, c.createdAt)
            FROM CheckIn c JOIN Habit h ON h.id = c.habitId
            WHERE c.userId = :userId
              AND (:habitId IS NULL OR c.habitId = :habitId)
              AND (CAST(:from AS LocalDate) IS NULL OR c.checkInDate >= :from)
              AND (CAST(:to AS LocalDate) IS NULL OR c.checkInDate <= :to)
            ORDER BY c.checkInDate DESC, c.id DESC
            """,
            countQuery = """
            SELECT COUNT(c) FROM CheckIn c
            WHERE c.userId = :userId
              AND (:habitId IS NULL OR c.habitId = :habitId)
              AND (CAST(:from AS LocalDate) IS NULL OR c.checkInDate >= :from)
              AND (CAST(:to AS LocalDate) IS NULL OR c.checkInDate <= :to)
            """)
    Page<CheckInHistoryItem> findHistory(@Param("userId") Long userId, @Param("habitId") Long habitId,
                                         @Param("from") LocalDate from, @Param("to") LocalDate to, Pageable pageable);
}
