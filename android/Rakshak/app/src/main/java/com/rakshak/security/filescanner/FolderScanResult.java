package com.rakshak.security.filescanner;

public class FolderScanResult {

    private final FileScanResult.ThreatLevel threatLevel;
    private final int totalFiles;
    private final String message;

    public FolderScanResult(
            FileScanResult.ThreatLevel threatLevel,
            int totalFiles,
            String message
    ) {
        this.threatLevel = threatLevel;
        this.totalFiles = totalFiles;
        this.message = message;
    }

    public FileScanResult.ThreatLevel getThreatLevel() {
        return threatLevel;
    }

    public int getTotalFiles() {
        return totalFiles;
    }

    public String getMessage() {
        return message;
    }
}
