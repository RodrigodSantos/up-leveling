package io.github.rodrigodsantos.upleveling.auth.dto;

import java.time.Instant;

/** O frontend usa o expiresAt para saber quando pedir um novo login. */
public record TokenResponse(String token, String type, Instant expiresAt) {
}
