package io.github.rodrigodsantos.upleveling.gamification;

/**
 * Curva de níveis: para chegar ao nível n são precisos 50 × n × (n − 1) de XP total.
 * Nível 2 = 100, 3 = 300, 4 = 600, 5 = 1000: cada nível pede 100 XP a mais que o anterior.
 * <p>
 * Funções puras (só dependem dos parâmetros), por isso static: não há estado nem nada externo para trocar em teste.
 */
public final class Levels {

    private Levels() {
    }

    public static long xpToReach(int level) {
        return 50L * level * (level - 1);
    }

    public static int levelFor(long totalXp) {
        int level = 1;
        while (xpToReach(level + 1) <= totalXp) {
            level++;
        }
        return level;
    }
}
