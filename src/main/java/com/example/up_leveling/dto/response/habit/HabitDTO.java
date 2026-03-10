package com.example.up_leveling.dto.response.habit;

import com.example.up_leveling.entity.Habit;
import com.example.up_leveling.entity.HabitSchedule;
import com.example.up_leveling.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HabitDTO {

    private Integer id;
    private String name;
    private Integer xpReward;
    private Status status;
    private List<HabitSchedule> schedules;

    public static HabitDTO fromEntity(Habit habit) {
        if (habit == null) return null;

        return HabitDTO.builder()
                .id(habit.getId())
                .name(habit.getName())
                .xpReward(habit.getXpReward())
                .status(habit.getStatus())
                .schedules(habit.getSchedules())
                .build();
    }

    public static List<HabitDTO> fromEntityList(List<Habit> habits) {
        if (habits == null) return Collections.emptyList();
        return habits.stream()
                .map(HabitDTO::fromEntity)
                .toList();
    }
}
