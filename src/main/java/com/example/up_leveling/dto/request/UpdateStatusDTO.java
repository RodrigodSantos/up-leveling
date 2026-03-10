package com.example.up_leveling.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateStatusDTO {

    @NotBlank(message = "O status não pode ser vazio")
    @Pattern(
            regexp = "^(ACTIVE|INACTIVE)$",
            message = "O status deve ser exatamente 'ACTIVE' ou 'INACTIVE'"
    )
    public String status;

}
