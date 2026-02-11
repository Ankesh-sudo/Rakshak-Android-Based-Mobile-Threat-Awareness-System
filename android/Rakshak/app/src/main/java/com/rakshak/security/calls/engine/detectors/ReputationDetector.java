package com.rakshak.security.calls.engine.detectors;

import com.rakshak.security.calls.data.ReputationDatabase;

public class ReputationDetector {

    public static int analyze(String number) {

        if (ReputationDatabase.isBlacklisted(number))
            return 60;

        return 0;
    }
}
