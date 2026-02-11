package com.rakshak.security.linkscanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Advanced heuristic-based link risk analysis engine.
 * Offline, explainable, India-focused.
 */
public class LinkRiskEngine {

    public static LinkRiskResult analyze(String originalUrl) {

        int score = 0;
        List<String> reasons = new ArrayList<>();

        // =================================================
        // 0. NULL / EMPTY SAFETY
        // =================================================
        if (originalUrl == null || originalUrl.trim().isEmpty()) {
            return new LinkRiskResult(
                    originalUrl,
                    originalUrl,
                    0,
                    LinkRiskResult.RiskLevel.SAFE,
                    reasons
            );
        }

        String url = originalUrl.trim();
        String lower = url.toLowerCase();
        String finalUrl = url;

        // =================================================
        // 1. URL SHORTENER DETECTION + RESOLUTION
        // =================================================
        if (UrlUnshortener.isShortenedUrl(lower)) {
            score += 30;
            reasons.add("Shortened link hides real destination");

            String resolved = UrlUnshortener.resolveFinalUrl(url);
            if (resolved != null && !resolved.isEmpty()
                    && !resolved.equalsIgnoreCase(url)) {
                finalUrl = resolved;
                reasons.add("Link redirects to a different destination");
            }
        }

        String finalLower = finalUrl.toLowerCase();

        // =================================================
        // 2. IP-BASED URL
        // =================================================
        if (finalLower.matches("https?://\\d+\\.\\d+\\.\\d+\\.\\d+.*")) {
            score += 40;
            reasons.add("Uses IP address instead of domain name");
        }

        // =================================================
        // 3. SUSPICIOUS DOMAIN EXTENSIONS
        // =================================================
        if (containsAny(finalLower,
                ".xyz", ".top", ".click", ".live",
                ".loan", ".work", ".shop")) {

            score += 25;
            reasons.add("Suspicious or commonly abused domain extension");
        }

        // =================================================
        // 4. LANDING PAGE / PHISHING PATTERNS
        // =================================================
        if (containsAny(finalLower,
                "/lp/", "/verify/", "/claim/",
                "/offer/", "/reward/", "/free/")) {

            score += 20;
            reasons.add("Suspicious landing page pattern detected");
        }

        // =================================================
        // 5. APK / DIRECT APP INSTALL
        // =================================================
        if (finalLower.endsWith(".apk")) {
            score += 50;
            reasons.add("APK download outside Google Play Store");
        }

        // =================================================
        // 6. INDIAN BANK IMPERSONATION
        // =================================================
        if (containsAny(finalLower,
                "sbi", "hdfc", "icici", "axis", "kotak",
                "pnb", "bob", "canara", "unionbank", "indusind")
                && !containsAny(finalLower, ".bank", ".com", ".co.in")) {

            score += 40;
            reasons.add("Possible fake Indian bank website");
        }

        // =================================================
        // 7. UPI / KYC / REFUND SCAMS
        // =================================================
        if (containsAny(finalLower,
                "upi", "kyc", "verify",
                "refund", "reward", "cashback")) {

            score += 25;
            reasons.add("Common UPI / KYC scam keywords detected");
        }

        // =================================================
        // 8. GOVERNMENT AUTHORITY MISUSE
        // =================================================
        if (containsAny(finalLower,
                "rbi", "npci", "uidai", "aadhaar",
                "incometax", "gov")
                && !containsAny(finalLower, ".gov.in")) {

            score += 35;
            reasons.add("Government authority name used in non-official domain");
        }

        // =================================================
        // 9. URL LENGTH HEURISTIC
        // =================================================
        if (finalUrl.length() > 120) {
            score += 25;
            reasons.add("Unusually long and obfuscated link structure");
        }

        // =================================================
        // 10. RANDOM TOKEN / OBFUSCATED PATH
        // =================================================
        String path = finalLower.replaceFirst("https?://[^/]+", "");
        if (path.matches(".*[a-z0-9]{25,}.*")) {
            score += 20;
            reasons.add("Randomized URL path commonly used in scam links");
        }

        // =================================================
        // 11. SHANNON ENTROPY (RANDOMNESS ANALYSIS)
        // =================================================
        double entropy = calculateEntropy(path);

        if (entropy > 4.2) {
            score += 30;
            reasons.add("Highly random link structure (possible phishing or tracking)");
        } else if (entropy > 3.8) {
            score += 20;
            reasons.add("Moderately obfuscated link structure");
        }

        // =================================================
        // 12. HTTP (NO TLS)
        // =================================================
        if (finalLower.startsWith("http://")) {
            score += 15;
            reasons.add("Link does not use secure HTTPS connection");
        }

        // =================================================
        // FINAL RISK LEVEL
        // =================================================
        LinkRiskResult.RiskLevel level;
        if (score >= 70) {
            level = LinkRiskResult.RiskLevel.DANGEROUS;
        } else if (score >= 35) {
            level = LinkRiskResult.RiskLevel.SUSPICIOUS;
        } else {
            level = LinkRiskResult.RiskLevel.SAFE;
        }

        return new LinkRiskResult(
                originalUrl,
                finalUrl,
                score,
                level,
                reasons
        );
    }

    // =================================================
    // SHANNON ENTROPY CALCULATION
    // =================================================
    private static double calculateEntropy(String input) {
        if (input == null || input.isEmpty()) return 0.0;

        Map<Character, Integer> freq = new HashMap<>();
        for (char c : input.toCharArray()) {
            freq.put(c, freq.getOrDefault(c, 0) + 1);
        }

        double entropy = 0.0;
        int len = input.length();

        for (int count : freq.values()) {
            double p = (double) count / len;
            entropy -= p * (Math.log(p) / Math.log(2));
        }

        return entropy;
    }

    // =================================================
    // HELPER
    // =================================================
    private static boolean containsAny(String text, String... keys) {
        if (text == null) return false;
        for (String k : keys) {
            if (text.contains(k)) return true;
        }
        return false;
    }
}
