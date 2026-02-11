package com.rakshak.security.calls.engine.detectors;

public class CountryCodeDetector {

    public static int analyze(String number) {

        if (number.startsWith("+234") || number.startsWith("+92"))
            return 35;

        return 0;
    }
}
