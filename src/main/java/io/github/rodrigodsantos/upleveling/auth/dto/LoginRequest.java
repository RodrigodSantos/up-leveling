package io.github.rodrigodsantos.upleveling.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** No login, só exige que venha algo: regras de formato aqui revelariam detalhes das contas. */
public record LoginRequest(
        @NotBlank(message = "é obrigatório")
        String email,

        @NotBlank(message = "é obrigatória")
        String password
) {
}
