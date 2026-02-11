package com.rakshak.security.health;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MediaStoreScanner {

    // ================= CALLBACK =================

    public interface ScanCallback {
        void onStatus(String status);
    }

    // ================= FILE MODEL =================

    public static class ScanFile implements Serializable {

        public String name;
        public String path;
        public String reason;
        public long size;

        public ScanFile(String name,
                        String path,
                        String reason,
                        long size) {
            this.name = name;
            this.path = path;
            this.reason = reason;
            this.size = size;
        }
    }

    // ================= SCAN RESULT =================

    public static class ScanResult {

        public int totalFiles = 0;

        public int suspiciousFiles = 0;
        public int hiddenFiles = 0;
        public int largeFiles = 0;

        // 🔴 Security threats
        public List<ScanFile> securityThreats = new ArrayList<>();

        // 🟠 Storage issues
        public List<ScanFile> storageIssues = new ArrayList<>();
    }

    // ================= MAIN SCAN =================

    public static ScanResult scanDevice(Context context,
                                        ScanCallback callback) {

        ScanResult result = new ScanResult();
        ContentResolver resolver = context.getContentResolver();

        // Prevent duplicate entries across collections
        Set<String> processedFiles = new HashSet<>();

        if (callback != null)
            callback.onStatus("Scanning Media Files...");

        scanCollection(resolver,
                MediaStore.Files.getContentUri("external"),
                result,
                processedFiles);

        return result;
    }

    // ================= COLLECTION SCAN =================

    private static void scanCollection(ContentResolver resolver,
                                       Uri collection,
                                       ScanResult result,
                                       Set<String> processedFiles) {

        String[] projection;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            projection = new String[]{
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    MediaStore.MediaColumns.SIZE,
                    MediaStore.MediaColumns.RELATIVE_PATH
            };
        } else {
            projection = new String[]{
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    MediaStore.MediaColumns.SIZE
            };
        }

        Cursor cursor = resolver.query(
                collection,
                projection,
                null,
                null,
                null
        );

        if (cursor == null)
            return;

        int nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
        int sizeIndex = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE);

        int pathIndex = -1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            pathIndex =
                    cursor.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH);
        }

        while (cursor.moveToNext()) {

            String name =
                    nameIndex >= 0 ? cursor.getString(nameIndex) : null;

            long size =
                    sizeIndex >= 0 ? cursor.getLong(sizeIndex) : 0;

            String path = "Unknown";
            if (pathIndex >= 0) {
                path = cursor.getString(pathIndex);
            }

            if (name == null)
                continue;

            // Avoid duplicate file processing
            String uniqueKey = name + "_" + path;
            if (processedFiles.contains(uniqueKey))
                continue;

            processedFiles.add(uniqueKey);

            result.totalFiles++;

            String lower = name.toLowerCase();

            // =====================================================
            // 🔴 SECURITY THREATS
            // =====================================================

            // Suspicious executable/script files
            if (lower.endsWith(".exe") ||
                    lower.endsWith(".bat") ||
                    lower.endsWith(".cmd") ||
                    lower.endsWith(".js") ||
                    lower.endsWith(".scr")) {

                result.suspiciousFiles++;
                result.securityThreats.add(
                        new ScanFile(name, path,
                                "Suspicious Executable", size)
                );
                continue;
            }

            // Double extension (photo.jpg.exe)
            if (lower.matches(".*\\.(jpg|png|pdf|doc|mp4)\\.(exe|bat|apk)$")) {

                result.suspiciousFiles++;
                result.securityThreats.add(
                        new ScanFile(name, path,
                                "Double Extension Attack", size)
                );
                continue;
            }

            // Unknown APK outside Downloads
            if (lower.endsWith(".apk")
                    && !path.toLowerCase().contains("download")) {

                result.suspiciousFiles++;
                result.securityThreats.add(
                        new ScanFile(name, path,
                                "Unknown APK Location", size)
                );
                continue;
            }

            // =====================================================
            // 🟠 STORAGE ISSUES
            // =====================================================

            // Hidden file
            if (name.startsWith(".")) {

                result.hiddenFiles++;
                result.storageIssues.add(
                        new ScanFile(name, path,
                                "Hidden File", size)
                );
            }

            // Large file (>100MB)
            if (size > 100L * 1024 * 1024) {

                result.largeFiles++;
                result.storageIssues.add(
                        new ScanFile(name, path,
                                "Large File (>100MB)", size)
                );
            }
        }

        cursor.close();
    }
}
