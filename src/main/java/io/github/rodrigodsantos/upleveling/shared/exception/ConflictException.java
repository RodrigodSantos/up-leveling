package io.github.rodrigodsantos.upleveling.shared.exception;

/**
 * A requisição colide com algo que já existe (ex.: e-mail já cadastrado). Responde 409.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
