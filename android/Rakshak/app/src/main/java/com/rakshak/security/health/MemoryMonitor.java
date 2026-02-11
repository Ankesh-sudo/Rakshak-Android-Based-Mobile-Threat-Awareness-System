package com.rakshak.security.health;

import android.app.ActivityManager;
import android.content.Context;

public class MemoryMonitor {

    public static float getUsedRamPercentage(Context context) {

        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        ActivityManager.MemoryInfo memoryInfo =
                new ActivityManager.MemoryInfo();

        activityManager.getMemoryInfo(memoryInfo);

        long total = memoryInfo.totalMem;
        long available = memoryInfo.availMem;

        return ((total - available) * 100f) / total;
    }
}
