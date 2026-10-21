package io.github.rodrigodsantos.upleveling.friendship.dto;

import java.util.List;

/** Os pedidos pendentes dos dois lados: os que chegaram (para aceitar ou recusar) e os que eu mandei. */
public record FriendRequestsResponse(List<FriendRequestResponse> received, List<FriendRequestResponse> sent) {
}
