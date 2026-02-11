package com.rakshak.security.calls.engine.detectors;

public class PatternDetector {

    public static int analyze(String number) {

        int score = 0;

        if (number.length() < 8)
            score += 40;

        if (number.matches("(\\d)\\1{6,}"))
            score += 30;

        if (number.matches("1234567.*"))
            score += 25;

        return score;
    }
}
