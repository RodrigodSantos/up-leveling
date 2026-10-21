package io.github.rodrigodsantos.upleveling.friendship.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body do POST /api/friends/requests: o código que o amigo passou (com ou sem o "UPL-"). */
public record FriendRequestCreate(
        @NotBlank(message = "é obrigatório")
        @Size(max = 20, message = "deve ter no máximo 20 caracteres")
        String friendCode
) {
}
