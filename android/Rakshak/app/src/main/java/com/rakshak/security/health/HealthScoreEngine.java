package com.rakshak.security.health;

public class HealthScoreEngine {

    public static int calculateScore(float battery,
                                     float temperature,
                                     float ram,
                                     float storage) {

        int score = 100;

        if (battery < 20) score -= 15;
        if (temperature > 45) score -= 25;
        if (ram > 80) score -= 20;
        if (storage > 90) score -= 20;

        return Math.max(score, 0);
    }
}
