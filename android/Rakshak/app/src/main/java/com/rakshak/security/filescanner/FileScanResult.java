package com.rakshak.security.filescanner;

public class FileScanResult {

    public enum ThreatLevel {
        SAFE,
        SUSPICIOUS,
        MALICIOUS
    }

    private ThreatLevel threatLevel;
    private String description;

    public FileScanResult(ThreatLevel threatLevel, String description) {
        this.threatLevel = threatLevel;
        this.description = description;
    }

    public ThreatLevel getThreatLevel() {
        return threatLevel;
    }

    public String getDescription() {
        return description;
    }
}
