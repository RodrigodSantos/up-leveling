package io.github.rodrigodsantos.upleveling.habit;

import io.github.rodrigodsantos.upleveling.habit.dto.HabitRequest;
import io.github.rodrigodsantos.upleveling.habit.dto.HabitResponse;
import io.github.rodrigodsantos.upleveling.shared.exception.BusinessRuleException;
import io.github.rodrigodsantos.upleveling.shared.exception.ConflictException;
import io.github.rodrigodsantos.upleveling.shared.exception.ResourceNotFoundException;
import io.github.rodrigodsantos.upleveling.shared.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static io.github.rodrigodsantos.upleveling.habit.HabitStatus.ACTIVE;
import static io.github.rodrigodsantos.upleveling.habit.HabitStatus.DELETED;
import static io.github.rodrigodsantos.upleveling.habit.HabitStatus.PAUSED;

@Service
public class HabitService {

    private final HabitRepository repository;
    private final CurrentUser currentUser;

    public HabitService(HabitRepository repository, CurrentUser currentUser) {
        this.repository = repository;
        this.currentUser = currentUser;
    }

    /** Sem filtro, traz ativos e pausados. Excluídos nunca são listados. */
    @Transactional(readOnly = true)
    public List<HabitResponse> list(HabitStatus status) {
        if (status == DELETED) {
            throw new BusinessRuleException("Hábitos excluídos não podem ser listados");
        }
        List<HabitStatus> statuses = status == null ? List.of(ACTIVE, PAUSED) : List.of(status);
        return repository.findByUserIdAndStatusInOrderByNameAsc(currentUser.id(), statuses).stream()
                .map(HabitResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public HabitResponse get(Long id) {
        return HabitResponse.from(findOwn(id));
    }

    @Transactional
    public HabitResponse create(HabitRequest request) {
        Long userId = currentUser.id();
        if (repository.existsByUserIdAndNameIgnoreCaseAndStatusNot(userId, request.name(), DELETED)) {
            throw duplicateName(request.name());
        }
        Habit habit = repository.save(new Habit(userId, request.name(), request.xpReward(), request.days()));
        return HabitResponse.from(habit);
    }

    @Transactional
    public HabitResponse update(Long id, HabitRequest request) {
        Habit habit = findOwn(id);
        if (repository.existsByUserIdAndNameIgnoreCaseAndStatusNotAndIdNot(habit.getUserId(), request.name(), DELETED, id)) {
            throw duplicateName(request.name());
        }
        habit.update(request.name(), request.xpReward(), request.days());
        return HabitResponse.from(habit);
    }

    /** Idempotente: pausar um hábito já pausado não é erro, só não muda nada. */
    @Transactional
    public HabitResponse changeStatus(Long id, HabitStatus status) {
        if (status == DELETED) {
            throw new BusinessRuleException("Para excluir um hábito, use DELETE /api/habits/{id}");
        }
        Habit habit = findOwn(id);
        habit.changeStatus(status);
        return HabitResponse.from(habit);
    }

    @Transactional
    public void delete(Long id) {
        findOwn(id).delete();
    }

    /** De outro usuário, inexistente ou excluído: os três respondem 404, sem revelar qual foi o caso. */
    private Habit findOwn(Long id) {
        return repository.findByIdAndUserIdAndStatusNot(id, currentUser.id(), DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("Hábito", id));
    }

    private ConflictException duplicateName(String name) {
        return new ConflictException("Você já tem um hábito chamado '" + name + "'");
    }
}
