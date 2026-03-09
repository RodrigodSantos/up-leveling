package com.example.up_leveling.dto.request.habit;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SaveHabitDTO {

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, max = 255, message = "O nome deve ter entre 3 e 100 caracteres")
    private String name;

    @NotNull(message = "A recompensa em xp é obrigatória")
    @Min(value = 1, message = "A recompensa em xp deve ser maior que um")
    private Integer xpReward;

    @NotNull(message = "É necessário informar o usuário ao qual o hábito pertence")
    @Min(value = 1, message = "O ID do usuário informado é inválido")
    private Integer userId;
}
