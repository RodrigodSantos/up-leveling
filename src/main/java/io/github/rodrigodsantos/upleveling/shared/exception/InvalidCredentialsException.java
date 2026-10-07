package io.github.rodrigodsantos.upleveling.shared.exception;

/**
 * Sempre a mesma mensagem, seja e-mail inexistente ou senha errada: não revela quais e-mails têm conta.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("E-mail ou senha inválidos");
    }
}
