package com.rakshak.security.permissions;

import android.Manifest;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PermissionAnalyzer {

    public static List<String> analyze(PackageManager pm, String packageName) {

        Set<String> grantedPermissions = new HashSet<>();

        try {
            PackageInfo pkgInfo = pm.getPackageInfo(
                    packageName,
                    PackageManager.GET_PERMISSIONS
            );

            if (pkgInfo.requestedPermissions == null ||
                    pkgInfo.requestedPermissionsFlags == null) {
                return new ArrayList<>();
            }

            for (int i = 0; i < pkgInfo.requestedPermissions.length; i++) {

                String permission = pkgInfo.requestedPermissions[i];
                int flags = pkgInfo.requestedPermissionsFlags[i];

                // ✅ ONLY include GRANTED permissions
                boolean isGranted =
                        (flags & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0;

                if (!isGranted) continue;

                // 📸 Camera
                if (Manifest.permission.CAMERA.equals(permission)) {
                    grantedPermissions.add(permission);
                }

                // 🎙️ Microphone
                else if (Manifest.permission.RECORD_AUDIO.equals(permission)) {
                    grantedPermissions.add(permission);
                }

                // 📍 Location
                else if (Manifest.permission.ACCESS_FINE_LOCATION.equals(permission)
                        || Manifest.permission.ACCESS_COARSE_LOCATION.equals(permission)) {
                    grantedPermissions.add(permission);
                }

                // ✉️ SMS
                else if (Manifest.permission.READ_SMS.equals(permission)
                        || Manifest.permission.SEND_SMS.equals(permission)
                        || Manifest.permission.RECEIVE_SMS.equals(permission)) {
                    grantedPermissions.add(permission);
                }

                // 👥 Contacts
                else if (Manifest.permission.READ_CONTACTS.equals(permission)
                        || Manifest.permission.WRITE_CONTACTS.equals(permission)) {
                    grantedPermissions.add(permission);
                }
            }

        } catch (PackageManager.NameNotFoundException ignored) {}

        return new ArrayList<>(grantedPermissions);
    }
}
