package com.rakshak.security.analyzers;

import android.content.Context;
import android.net.Uri;
import com.rakshak.security.filescanner.FileScanResult;

public class ArchiveAnalyzer {

    public static FileScanResult analyze(Context context, Uri uri) {

        return new FileScanResult(
                FileScanResult.ThreatLevel.SUSPICIOUS,
                "Archive file detected. Hidden files may exist."
        );
    }
}
