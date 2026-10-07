package io.github.rodrigodsantos.upleveling.gamification.dto;

import io.github.rodrigodsantos.upleveling.gamification.Levels;

/**
 * Tudo que o frontend precisa para desenhar a barra de nível.
 * Ex.: 150 XP → nível 2, barra entre 100 (início do nível 2) e 300 (nível 3), 25% preenchida.
 */
public record ProgressResponse(long totalXp, int level, long currentLevelXp, long nextLevelXp, int progressPercent) {

    public static ProgressResponse of(long totalXp) {
        int level = Levels.levelFor(totalXp);
        long currentLevelXp = Levels.xpToReach(level);
        long nextLevelXp = Levels.xpToReach(level + 1);
        int percent = (int) ((totalXp - currentLevelXp) * 100 / (nextLevelXp - currentLevelXp));
        return new ProgressResponse(totalXp, level, currentLevelXp, nextLevelXp, percent);
    }
}
