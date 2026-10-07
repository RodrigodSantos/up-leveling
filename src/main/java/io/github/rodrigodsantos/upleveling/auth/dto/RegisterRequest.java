package io.github.rodrigodsantos.upleveling.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "é obrigatório")
        @Size(max = 100, message = "deve ter no máximo 100 caracteres")
        String name,

        @NotBlank(message = "é obrigatório")
        @Email(message = "deve ser um e-mail válido")
        @Size(max = 150, message = "deve ter no máximo 150 caracteres")
        String email,

        // O BCrypt ignora o que passa de 72 bytes: acima disso, senhas diferentes virariam a mesma
        @NotBlank(message = "é obrigatória")
        @Size(min = 8, max = 72, message = "deve ter entre 8 e 72 caracteres")
        String password
) {

    /**
     * Construtor compacto: roda ao criar o record, antes da validação. Tira os espaços das pontas
     * (comuns ao colar o e-mail num formulário), que senão seriam barrados pelo @Email.
     */
    public RegisterRequest {
        email = email == null ? null : email.trim();
    }
}
