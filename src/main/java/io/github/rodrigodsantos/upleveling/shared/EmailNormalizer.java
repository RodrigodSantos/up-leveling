package io.github.rodrigodsantos.upleveling.shared;

import java.util.Locale;

/**
 * " Ana@Mail.com " e "ana@mail.com" são a mesma conta: o e-mail é sempre gravado e buscado nesta forma.
 */
public final class EmailNormalizer {

    private EmailNormalizer() {
    }

    public static String normalize(String email) {
        // Locale.ROOT: em turco, "I".toLowerCase() vira "ı" (sem ponto) e quebraria a comparação
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
