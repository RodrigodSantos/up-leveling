package io.github.rodrigodsantos.upleveling.habit.dto;

import io.github.rodrigodsantos.upleveling.habit.HabitStatus;
import jakarta.validation.constraints.NotNull;

public record HabitStatusRequest(
        @NotNull(message = "é obrigatório (ACTIVE ou PAUSED)")
        HabitStatus status
) {
}
