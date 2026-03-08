package com.example.up_leveling.dto.request.user;

import com.example.up_leveling.entity.Status;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateStatusUserDTO {

    @NotBlank(message = "O status não pode ser vazio")
    @Pattern(
            regexp = "^(ACTIVE|INACTIVE)$",
            message = "O status deve ser exatamente 'ACTIVE' ou 'INACTIVE'"
    )
    public String status;
}
