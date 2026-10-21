package io.github.rodrigodsantos.upleveling.friendship.dto;

import io.github.rodrigodsantos.upleveling.gamification.dto.ProgressResponse;

import java.time.LocalDateTime;

/**
 * Um amigo na lista: nome e nível. Os hábitos continuam privados (e-mail também).
 * O progress é o mesmo da barra de nível do cabeçalho, para o frontend reaproveitar o desenho.
 */
public record FriendResponse(Long userId, String name, ProgressResponse progress, LocalDateTime friendsSince) {
}
