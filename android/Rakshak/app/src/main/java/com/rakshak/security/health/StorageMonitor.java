package com.rakshak.security.health;

import android.os.Environment;
import android.os.StatFs;

public class StorageMonitor {

    public static float getUsedStoragePercentage() {

        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());

        long totalBytes = stat.getTotalBytes();
        long freeBytes = stat.getAvailableBytes();

        return ((totalBytes - freeBytes) * 100f) / totalBytes;
    }
}
