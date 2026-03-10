package com.example.up_leveling.dto.request.habit;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateHabitDTO {

    @Pattern(regexp = "^$|^.{3,255}$", message = "O nome deve estar vazio ou ter entre 3 e 255 caracteres")
    private String name;

    @Min(value = 1, message = "A recompensa em xp deve ser maior que zero")
    private Integer xpReward;
}
