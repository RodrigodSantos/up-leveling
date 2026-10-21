package io.github.rodrigodsantos.upleveling.user;

import java.security.SecureRandom;
import java.util.Locale;

/**
 * Código que uma pessoa passa para a outra adicioná-la como amiga: "UPL-" + 6 caracteres (ex.: UPL-7K3Q9M).
 * <p>
 * 32 caracteres possíveis em 6 posições dão ~1 bilhão de códigos: a chance de sortear um repetido é desprezível,
 * e se acontecer, o índice único do banco recusa (o cadastro responde 409 e a pessoa tenta de novo).
 * SecureRandom em vez de Random: o código não pode ser adivinhado a partir dos anteriores.
 */
public final class FriendCode {

    static final String PREFIX = "UPL-";
    // Sem 0/O e 1/I, que se confundem quando alguém dita ou copia o código à mão (o mesmo da migration V7)
    private static final String ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private FriendCode() {
    }

    public static String generate() {
        StringBuilder code = new StringBuilder(PREFIX);
        for (int i = 0; i < LENGTH; i++) {
            code.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }

    /** Aceita como a pessoa digitar: " upl-7k3q9m ", "7K3Q9M" e "UPL-7K3Q9M" viram o mesmo código. */
    public static String normalize(String typed) {
        String code = typed.replaceAll("\s", "").toUpperCase(Locale.ROOT);
        return code.startsWith(PREFIX) ? code : PREFIX + code;
    }
}
