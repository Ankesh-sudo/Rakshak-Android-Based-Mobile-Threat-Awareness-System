package com.rakshak.security.analyzers;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.rakshak.security.filescanner.FileScanResult;

import java.util.Arrays;

public class ApkAnalyzer {

    public static FileScanResult analyze(Context context, Uri uri) {

        try {
            PackageManager pm = context.getPackageManager();

            PackageInfo info =
                    pm.getPackageArchiveInfo(
                            uri.getPath(),
                            PackageManager.GET_PERMISSIONS
                    );

            if (info != null && info.requestedPermissions != null) {

                for (String perm : info.requestedPermissions) {
                    if (perm.contains("READ_SMS")
                            || perm.contains("SEND_SMS")
                            || perm.contains("READ_CONTACTS")
                            || perm.contains("SYSTEM_ALERT_WINDOW")) {

                        return new FileScanResult(
                                FileScanResult.ThreatLevel.SUSPICIOUS,
                                "APK requests sensitive permissions: "
                                        + Arrays.toString(info.requestedPermissions)
                        );
                    }
                }
            }

        } catch (Exception ignored) {}

        return new FileScanResult(
                FileScanResult.ThreatLevel.SAFE,
                "APK does not request high-risk permissions."
        );
    }
}
