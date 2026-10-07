package io.github.rodrigodsantos.upleveling.habit.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.DayOfWeek;
import java.util.Set;

/** Usado no POST e no PUT: o PUT substitui o hábito inteiro, então os mesmos campos são obrigatórios. */
public record HabitRequest(
        @NotBlank(message = "é obrigatório")
        @Size(max = 100, message = "deve ter no máximo 100 caracteres")
        String name,

        @NotNull(message = "é obrigatório")
        @Min(value = 1, message = "deve estar entre 1 e 100")
        @Max(value = 100, message = "deve estar entre 1 e 100")
        Integer xpReward,

        // Vazio ou ausente = todos os dias
        Set<DayOfWeek> days
) {

    public HabitRequest {
        name = name == null ? null : name.trim();
        days = days == null ? Set.of() : Set.copyOf(days);
    }
}
