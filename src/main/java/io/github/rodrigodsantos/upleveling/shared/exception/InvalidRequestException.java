package io.github.rodrigodsantos.upleveling.shared.exception;

/**
 * Parâmetros que não fazem sentido juntos (ex.: data inicial depois da final). Responde 400.
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
