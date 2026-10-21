package io.github.rodrigodsantos.upleveling.user.dto;

import io.github.rodrigodsantos.upleveling.user.User;

import java.time.LocalDateTime;

/** Nunca devolve o hash da senha. O friendCode é o que a pessoa passa para os amigos a adicionarem. */
public record UserResponse(Long id, String name, String email, String friendCode, LocalDateTime createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getFriendCode(),
                user.getCreatedAt());
    }
}
