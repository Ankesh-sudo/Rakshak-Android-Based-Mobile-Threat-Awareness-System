package com.rakshak.security.filescanner;

import android.content.Context;
import android.net.Uri;

import com.rakshak.security.analyzers.*;

public class UniversalScanEngine {

    public static FileScanResult scan(Context context, Uri uri) {

        FileTypeDetector.FileType type =
                FileTypeDetector.detect(context, uri);

        switch (type) {

            case APK:
                return ApkAnalyzer.analyze(context, uri);

            case PDF:
                return DocumentAnalyzer.analyze(context, uri);

            case IMAGE:
            case VIDEO:
            case AUDIO:
                return MediaAnalyzer.analyze(context, uri);

            case ARCHIVE:
                return ArchiveAnalyzer.analyze(context, uri);

            default:
                return new FileScanResult(
                        FileScanResult.ThreatLevel.SUSPICIOUS,
                        "Unknown file type. Caution recommended."
                );
        }
    }
}
