package com.rakshak.security.permissions;

import java.util.List;

/**
 * Converts a list of dangerous permissions into a RiskLevel.
 * This class contains ONLY logic — no UI, no Android dependencies.
 */
public class RiskClassifier {

    public static RiskLevel classify(List<String> permissions) {

        // 🟢 SAFE: No dangerous permissions
        if (permissions == null || permissions.isEmpty()) {
            return RiskLevel.SAFE;
        }

        boolean hasCamera = permissions.contains("Camera");
        boolean hasMic = permissions.contains("Microphone");
        boolean hasSMS = permissions.contains("SMS");
        boolean hasLocation = permissions.contains("Location");

        int count = permissions.size();

        // 🔴 HIGH RISK CONDITIONS
        if (
                (hasCamera && hasMic) ||           // Silent surveillance
                        (hasSMS && count > 1) ||            // Financial / OTP abuse
                        (hasLocation && (hasCamera || hasMic)) || // Tracking + spying
                        (count >= 3)                        // Too many dangerous perms
        ) {
            return RiskLevel.HIGH;
        }

        // 🟠 CAUTION: Single dangerous permission
        return RiskLevel.CAUTION;
    }
}
