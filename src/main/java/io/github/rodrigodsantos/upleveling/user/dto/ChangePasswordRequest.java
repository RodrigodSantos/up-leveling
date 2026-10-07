package io.github.rodrigodsantos.upleveling.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "é obrigatória")
        String currentPassword,

        // O BCrypt ignora o que passa de 72 bytes: acima disso, senhas diferentes virariam a mesma
        @NotBlank(message = "é obrigatória")
        @Size(min = 8, max = 72, message = "deve ter entre 8 e 72 caracteres")
        String newPassword
) {
}
