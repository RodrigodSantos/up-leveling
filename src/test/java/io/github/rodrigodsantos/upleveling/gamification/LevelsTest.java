package io.github.rodrigodsantos.upleveling.gamification;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de unidade: sem Spring, sem banco. Levels é uma função pura, então basta chamar e conferir.
 */
class LevelsTest {

    @ParameterizedTest(name = "nível {0} pede {1} XP")
    @CsvSource({"1, 0", "2, 100", "3, 300", "4, 600", "5, 1000", "10, 4500"})
    void xpToReachGrowsBy100EachLevel(int level, long xp) {
        assertThat(Levels.xpToReach(level)).isEqualTo(xp);
    }

    @ParameterizedTest(name = "{0} XP = nível {1}")
    @CsvSource({"0, 1", "99, 1", "100, 2", "299, 2", "300, 3", "4499, 9", "4500, 10"})
    void levelForUsesTheLastLevelReached(long xp, int level) {
        assertThat(Levels.levelFor(xp)).isEqualTo(level);
    }
}
