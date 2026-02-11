package com.rakshak.security.filescanner;

import android.content.Context;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

public class FolderScanner {

    public static FolderScanResult scanFolder(
            Context context,
            Uri folderUri
    ) {

        DocumentFile folder;

        // ================= SAF / OEM SAFE CREATION =================
        try {
            folder = DocumentFile.fromTreeUri(context, folderUri);
        } catch (Exception e) {
            return new FolderScanResult(
                    FileScanResult.ThreatLevel.SUSPICIOUS,
                    0,
                    "Unable to access the selected folder."
            );
        }

        if (folder == null || !folder.isDirectory()) {
            return new FolderScanResult(
                    FileScanResult.ThreatLevel.SUSPICIOUS,
                    0,
                    "Invalid or inaccessible folder."
            );
        }

        int totalFiles = 0;
        int skippedFiles = 0;

        DocumentFile[] files;

        // ================= SAF PERMISSION SAFETY =================
        try {
            files = folder.listFiles();
        } catch (SecurityException e) {
            return new FolderScanResult(
                    FileScanResult.ThreatLevel.SUSPICIOUS,
                    0,
                    "Permission denied while scanning this folder."
            );
        }

        if (files == null || files.length == 0) {
            return new FolderScanResult(
                    FileScanResult.ThreatLevel.SAFE,
                    0,
                    "Folder is empty or contains no scannable files."
            );
        }

        // ================= SCAN FILES =================
        for (DocumentFile file : files) {

            if (file == null || !file.isFile()) {
                skippedFiles++;
                continue;
            }

            totalFiles++;

            FileScanResult result;

            try {
                result = UniversalScanEngine.scan(
                        context,
                        file.getUri()
                );
            } catch (Exception e) {
                // Skip unreadable file, never crash
                skippedFiles++;
                continue;
            }

            // 🚨 Immediate stop on malicious file
            if (result.getThreatLevel()
                    == FileScanResult.ThreatLevel.MALICIOUS) {

                return new FolderScanResult(
                        FileScanResult.ThreatLevel.MALICIOUS,
                        totalFiles,
                        "Malicious file detected inside the folder."
                );
            }
        }

        // ================= FINAL RESULT =================
        if (totalFiles == 0) {
            return new FolderScanResult(
                    FileScanResult.ThreatLevel.SAFE,
                    0,
                    "No accessible files found in the selected folder."
            );
        }

        if (skippedFiles > 0) {
            return new FolderScanResult(
                    FileScanResult.ThreatLevel.SAFE,
                    totalFiles,
                    "Folder scanned safely. Some files were skipped due to system restrictions."
            );
        }

        return new FolderScanResult(
                FileScanResult.ThreatLevel.SAFE,
                totalFiles,
                "Folder scanned successfully. No high-risk files found."
        );
    }
}
