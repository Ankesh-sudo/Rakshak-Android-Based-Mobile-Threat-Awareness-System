package com.rakshak.security.permissions;

/**
 * Represents the overall security risk level of an app
 * based on the dangerous permissions it uses.
 */
public enum RiskLevel {
    SAFE,      // No dangerous permissions
    CAUTION,   // One sensitive permission
    HIGH       // Multiple or critical combinations
}
