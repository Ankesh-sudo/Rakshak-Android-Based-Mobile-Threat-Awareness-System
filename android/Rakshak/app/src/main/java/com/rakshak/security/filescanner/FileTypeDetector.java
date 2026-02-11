package com.rakshak.security.filescanner;

import android.content.Context;
import android.net.Uri;

import java.io.InputStream;

public class FileTypeDetector {

    public enum FileType {
        APK,
        PDF,
        IMAGE,
        VIDEO,
        AUDIO,
        ARCHIVE,
        EXECUTABLE,
        DOCUMENT,
        UNKNOWN
    }

    public static FileType detect(Context context, Uri uri) {

        try (InputStream is =
                     context.getContentResolver().openInputStream(uri)) {

            if (is == null) return FileType.UNKNOWN;

            byte[] header = new byte[16];
            int read = is.read(header);
            if (read <= 0) return FileType.UNKNOWN;

            String hex = bytesToHex(header);

            // ================= ZIP BASED =================
            // ZIP / APK / DOCX / XLSX / PPTX
            if (hex.startsWith("504B0304")) {
                return FileType.ARCHIVE;
            }

            // ================= DOCUMENTS =================
            // PDF
            if (hex.startsWith("25504446")) {
                return FileType.PDF;
            }

            // ================= IMAGES =================
            if (hex.startsWith("FFD8")) return FileType.IMAGE;      // JPG
            if (hex.startsWith("89504E47")) return FileType.IMAGE;  // PNG
            if (hex.startsWith("47494638")) return FileType.IMAGE;  // GIF
            if (hex.startsWith("424D")) return FileType.IMAGE;      // BMP
            if (hex.startsWith("49492A00")) return FileType.IMAGE;  // TIFF
            if (hex.startsWith("4D4D002A")) return FileType.IMAGE;  // TIFF

            // ================= AUDIO =================
            if (hex.startsWith("494433")) return FileType.AUDIO;    // MP3
            if (hex.startsWith("52494646")) return FileType.AUDIO;  // WAV
            if (hex.startsWith("4F676753")) return FileType.AUDIO;  // OGG

            // ================= VIDEO =================
            if (hex.contains("66747970")) return FileType.VIDEO;    // MP4/MOV
            if (hex.startsWith("1A45DFA3")) return FileType.VIDEO;  // MKV/WebM
            if (hex.startsWith("52494646")) return FileType.VIDEO;  // AVI

            // ================= EXECUTABLE =================
            if (hex.startsWith("4D5A")) return FileType.EXECUTABLE; // Windows EXE

        } catch (Exception ignored) {
        }

        return FileType.UNKNOWN;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
