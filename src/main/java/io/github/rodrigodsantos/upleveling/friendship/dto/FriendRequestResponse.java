package io.github.rodrigodsantos.upleveling.friendship.dto;

import io.github.rodrigodsantos.upleveling.friendship.FriendshipStatus;

import java.time.LocalDateTime;

/**
 * Um pedido de amizade, visto por quem está logado: "user" é sempre a outra pessoa (quem mandou, nos recebidos;
 * para quem foi, nos enviados). O status só sai ACCEPTED quando o pedido aceitou um que a outra já tinha mandado.
 */
public record FriendRequestResponse(Long id, Long userId, String name, FriendshipStatus status,
                                    LocalDateTime createdAt) {
}
