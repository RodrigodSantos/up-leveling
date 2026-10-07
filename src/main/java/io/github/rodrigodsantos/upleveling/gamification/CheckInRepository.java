package io.github.rodrigodsantos.upleveling.gamification;

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

    /** XP total do usuário = soma dos check-ins. Hábitos excluídos continuam contando: o XP já foi conquistado. */
    @Query("SELECT COALESCE(SUM(c.xp + c.bonusXp), 0) FROM CheckIn c WHERE c.userId = :userId")
    long totalXp(@Param("userId") Long userId);
}
