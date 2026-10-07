package io.github.rodrigodsantos.upleveling.habit;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Toda consulta filtra pelo dono (userId): não existe caminho para ler o hábito de outro usuário.
 * O @EntityGraph traz os dias na mesma consulta; sem ele, listar 20 hábitos geraria 1 + 20 consultas (N+1).
 */
public interface HabitRepository extends JpaRepository<Habit, Long> {

    @EntityGraph(attributePaths = "days")
    List<Habit> findByUserIdAndStatusInOrderByNameAsc(Long userId, Collection<HabitStatus> statuses);

    @EntityGraph(attributePaths = "days")
    Optional<Habit> findByIdAndUserIdAndStatusNot(Long id, Long userId, HabitStatus status);

    boolean existsByUserIdAndNameIgnoreCaseAndStatusNot(Long userId, String name, HabitStatus status);

    /** Na edição: outro hábito meu, que não este, já usa o nome? */
    boolean existsByUserIdAndNameIgnoreCaseAndStatusNotAndIdNot(Long userId, String name, HabitStatus status, Long id);
}
