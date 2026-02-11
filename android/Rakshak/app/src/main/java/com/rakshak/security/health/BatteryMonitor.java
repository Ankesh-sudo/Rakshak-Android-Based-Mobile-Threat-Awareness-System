package com.rakshak.security.health;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class BatteryMonitor {

    public static float getBatteryPercentage(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        return (level * 100f) / scale;
    }

    public static float getBatteryTemperature(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        int temp = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
        return temp / 10f;
    }
}
