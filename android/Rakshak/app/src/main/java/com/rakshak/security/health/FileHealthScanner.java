package com.rakshak.security.health;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FileHealthScanner {

    public interface ScanCallback {
        void onStatusUpdate(String status);
    }

    public static class ThreatFile {
        public String path;
        public String reason;

        public ThreatFile(String path, String reason) {
            this.path = path;
            this.reason = reason;
        }
    }

    public static class ScanResult {
        public int totalFiles = 0;
        public int suspiciousFiles = 0;
        public int hiddenFiles = 0;
        public int largeFiles = 0;
        public List<ThreatFile> threatList = new ArrayList<>();
    }

    public static ScanResult scanPublicDirectories(ScanCallback callback) {

        ScanResult result = new ScanResult();

        File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File documents = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        scanDirectory(downloads, "Scanning Downloads...", callback, result);
        scanDirectory(documents, "Checking Documents...", callback, result);
        scanDirectory(pictures, "Analyzing Pictures...", callback, result);

        return result;
    }

    private static void scanDirectory(File dir, String status,
                                      ScanCallback callback,
                                      ScanResult result) {

        if (dir == null || !dir.exists() || !dir.canRead())
            return;

        if (callback != null)
            callback.onStatusUpdate(status);

        File[] files = dir.listFiles();
        if (files == null)
            return;

        for (File file : files) {

            if (file.isDirectory()) {
                scanDirectory(file, status, callback, result);
            } else {

                result.totalFiles++;

                String name = file.getName().toLowerCase(Locale.ROOT);

                if (file.isHidden()) {
                    result.hiddenFiles++;
                    result.threatList.add(new ThreatFile(file.getAbsolutePath(), "Hidden File"));
                }

                if (name.endsWith(".exe") ||
                        name.endsWith(".bat") ||
                        name.endsWith(".cmd") ||
                        name.endsWith(".js")) {

                    result.suspiciousFiles++;
                    result.threatList.add(new ThreatFile(file.getAbsolutePath(), "Suspicious Extension"));
                }

                if (name.matches(".*\\.(jpg|png|pdf|doc)\\.(exe|bat|apk)$")) {
                    result.suspiciousFiles++;
                    result.threatList.add(new ThreatFile(file.getAbsolutePath(), "Double Extension"));
                }

                if (file.length() > 100 * 1024 * 1024) {
                    result.largeFiles++;
                    result.threatList.add(new ThreatFile(file.getAbsolutePath(), "Large File"));
                }
            }
        }
    }
}
