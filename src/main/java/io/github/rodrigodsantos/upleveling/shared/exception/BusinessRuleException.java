package io.github.rodrigodsantos.upleveling.shared.exception;

/**
 * A requisição está bem formada, mas quebra uma regra do domínio (ex.: "este hábito já foi cumprido hoje").
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
