package io.github.rodrigodsantos.upleveling.habit.dto;

import io.github.rodrigodsantos.upleveling.habit.Habit;
import io.github.rodrigodsantos.upleveling.habit.HabitStatus;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

/** Os dias saem em ordem (segunda → domingo), para o frontend não precisar ordenar. */
public record HabitResponse(Long id, String name, int xpReward, HabitStatus status, List<DayOfWeek> days,
                            LocalDateTime createdAt) {

    public static HabitResponse from(Habit habit) {
        return new HabitResponse(
                habit.getId(),
                habit.getName(),
                habit.getXpReward(),
                habit.getStatus(),
                habit.getDays().stream().sorted().toList(),
                habit.getCreatedAt()
        );
    }
}
