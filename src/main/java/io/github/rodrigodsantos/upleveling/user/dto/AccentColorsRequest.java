package io.github.rodrigodsantos.upleveling.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * PUT substitui a lista inteira: para salvar uma cor, o frontend manda a lista com ela; para tirar, sem ela.
 * As validações nos elementos (List&lt;@Min Integer&gt;) valem para cada matiz da lista.
 */
public record AccentColorsRequest(
        @NotNull(message = "é obrigatório")
        @Size(max = 5, message = "pode ter no máximo 5 cores")
        List<@NotNull(message = "não pode ter valor vazio")
             @Min(value = 0, message = "cada cor deve ser uma matiz de 0 a 359")
             @Max(value = 359, message = "cada cor deve ser uma matiz de 0 a 359") Integer> hues
) {
}
