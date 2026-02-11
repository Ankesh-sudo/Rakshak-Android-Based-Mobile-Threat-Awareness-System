package com.rakshak.security.filescanner;

import android.content.Context;
import android.content.SharedPreferences;

public class ScanHistoryManager {

    private static final String PREF = "scan_history";

    public static void save(
            Context context,
            String file,
            String result
    ) {
        SharedPreferences sp =
                context.getSharedPreferences(PREF, Context.MODE_PRIVATE);

        sp.edit()
                .putString(
                        String.valueOf(System.currentTimeMillis()),
                        file + " → " + result
                )
                .apply();
    }
}
