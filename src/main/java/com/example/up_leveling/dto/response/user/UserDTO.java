package com.example.up_leveling.dto.response.user;

import com.example.up_leveling.dto.response.habit.HabitDTO;
import com.example.up_leveling.entity.Status;
import com.example.up_leveling.entity.User;
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
public class UserDTO {
    private Integer id;
    private String name;
    private String email;
    private Integer xpTotal;
    private Status status;
    private List<HabitDTO> habits;

    public static UserDTO fromEntity(User user) {
        if (user == null) return null;

        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .status(user.getStatus())
                .habits(HabitDTO.fromEntityList(user.getHabits()))
                .build();
    }

    public static List<UserDTO> fromEntityList(List<User> habits) {
        if (habits == null) return Collections.emptyList();
        return habits.stream()
                .map(UserDTO::fromEntity)
                .toList();
    }
}
