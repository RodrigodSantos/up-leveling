package io.github.rodrigodsantos.upleveling.user.dto;

import java.util.List;

/** Cores salvas, na ordem em que foram salvas (matizes de 0 a 359°). */
public record AccentColorsResponse(List<Integer> hues) {
}
