package com.rakshak.security.calls.engine;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.provider.ContactsContract;

import java.util.Calendar;

public class CallFeatureExtractor {

    // ======================================
    // 1️⃣ Call Frequency (last 7 days)
    // ======================================
    public static int getCallFrequency(Context context, String number) {

        int count = 0;

        long oneWeekAgo =
                System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);

        Cursor cursor = context.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                null,
                CallLog.Calls.NUMBER + "=? AND " +
                        CallLog.Calls.DATE + ">=?",
                new String[]{number, String.valueOf(oneWeekAgo)},
                null
        );

        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();
        }

        return count;
    }

    // ======================================
    // 2️⃣ Average Call Duration
    // ======================================
    public static int getAverageCallDuration(Context context, String number) {

        int totalDuration = 0;
        int count = 0;

        Cursor cursor = context.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                null,
                CallLog.Calls.NUMBER + "=?",
                new String[]{number},
                null
        );

        if (cursor != null) {

            int durationIndex =
                    cursor.getColumnIndex(CallLog.Calls.DURATION);

            while (cursor.moveToNext()) {
                totalDuration += cursor.getInt(durationIndex);
                count++;
            }

            cursor.close();
        }

        if (count == 0) return 0;

        return totalDuration / count;
    }

    // ======================================
    // 3️⃣ Night Call Detection
    // ======================================
    public static int isNightCall() {

        int hour =
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        return (hour >= 23 || hour <= 5) ? 1 : 0;
    }

    // ======================================
    // 4️⃣ Unknown Number Check
    // ======================================
    public static int isUnknownNumber(Context context, String number) {

        Cursor cursor = context.getContentResolver().query(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI
                        .buildUpon()
                        .appendPath(number)
                        .build(),
                null,
                null,
                null,
                null
        );

        boolean exists =
                (cursor != null && cursor.getCount() > 0);

        if (cursor != null) cursor.close();

        return exists ? 0 : 1;
    }

    // ======================================
    // 5️⃣ Short Calls Pattern (< 10 sec)
    // ======================================
    public static int getShortCallCount(Context context, String number) {

        int shortCalls = 0;

        Cursor cursor = context.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                null,
                CallLog.Calls.NUMBER + "=?",
                new String[]{number},
                null
        );

        if (cursor != null) {

            int durationIndex =
                    cursor.getColumnIndex(CallLog.Calls.DURATION);

            while (cursor.moveToNext()) {

                int duration =
                        cursor.getInt(durationIndex);

                if (duration < 10) {
                    shortCalls++;
                }
            }

            cursor.close();
        }

        return shortCalls;
    }
}
