package io.github.rodrigodsantos.upleveling.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** PUT substitui o perfil inteiro: os dois campos são obrigatórios (não existe "campo ausente" para tratar). */
public record UpdateProfileRequest(
        @NotBlank(message = "é obrigatório")
        @Size(max = 100, message = "deve ter no máximo 100 caracteres")
        String name,

        @NotBlank(message = "é obrigatório")
        @Email(message = "deve ser um e-mail válido")
        @Size(max = 150, message = "deve ter no máximo 150 caracteres")
        String email
) {

    /** Tira os espaços das pontas antes da validação (ver RegisterRequest). */
    public UpdateProfileRequest {
        email = email == null ? null : email.trim();
    }
}
