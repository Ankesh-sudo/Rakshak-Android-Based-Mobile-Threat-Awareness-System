package com.rakshak.security.health;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

public class MediaStoreScanner {

    public interface ScanCallback {
        void onStatus(String status);
    }

    public static class ThreatFile {
        public String name;
        public String path;
        public String reason;

        public ThreatFile(String name, String path, String reason) {
            this.name = name;
            this.path = path;
            this.reason = reason;
        }
    }

    public static class ScanResult {
        public int totalFiles = 0;
        public int suspiciousFiles = 0;
        public int largeFiles = 0;
        public List<ThreatFile> threats = new ArrayList<>();
    }

    public static ScanResult scanDevice(Context context, ScanCallback callback) {

        ScanResult result = new ScanResult();
        ContentResolver resolver = context.getContentResolver();

        // Scan Downloads / Files
        if (callback != null) callback.onStatus("Scanning Downloads...");
        scanCollection(resolver, MediaStore.Files.getContentUri("external"), result);

        // Scan Images
        if (callback != null) callback.onStatus("Scanning Images...");
        scanCollection(resolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, result);

        // Scan Videos
        if (callback != null) callback.onStatus("Scanning Videos...");
        scanCollection(resolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, result);

        // Scan Audio
        if (callback != null) callback.onStatus("Scanning Audio...");
        scanCollection(resolver, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, result);

        return result;
    }

    private static void scanCollection(ContentResolver resolver,
                                       Uri collection,
                                       ScanResult result) {

        String[] projection = {
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.DATA
        };

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
        int pathIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);

        while (cursor.moveToNext()) {

            result.totalFiles++;

            String name = cursor.getString(nameIndex);
            long size = cursor.getLong(sizeIndex);
            String path = pathIndex >= 0 ? cursor.getString(pathIndex) : "Unknown";

            if (name == null) continue;

            String lower = name.toLowerCase();

            // Suspicious extensions
            if (lower.endsWith(".exe") ||
                    lower.endsWith(".bat") ||
                    lower.endsWith(".cmd") ||
                    lower.endsWith(".js")) {

                result.suspiciousFiles++;
                result.threats.add(
                        new ThreatFile(name, path, "Suspicious Extension")
                );
            }

            // Double extension
            if (lower.matches(".*\\.(jpg|png|pdf|doc)\\.(exe|bat|apk)$")) {
                result.suspiciousFiles++;
                result.threats.add(
                        new ThreatFile(name, path, "Double Extension")
                );
            }

            // Large file > 100MB
            if (size > 100 * 1024 * 1024) {
                result.largeFiles++;
                result.threats.add(
                        new ThreatFile(name, path, "Large File")
                );
            }
        }

        cursor.close();
    }
}
