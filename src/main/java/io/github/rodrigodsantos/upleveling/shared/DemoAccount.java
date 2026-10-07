package io.github.rodrigodsantos.upleveling.shared;

/**
 * Conta pública de demonstração (aparece no README e no Swagger): quem visita o deploy entra e já vê dados.
 */
public final class DemoAccount {

    public static final String EMAIL = "demo@upleveling.local";
    public static final String PASSWORD = "demo1234";

    private DemoAccount() {
    }

    public static boolean is(String email) {
        return EMAIL.equals(email);
    }
}
