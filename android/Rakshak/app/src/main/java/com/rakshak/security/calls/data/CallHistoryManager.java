package com.rakshak.security.calls.data;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;

public class CallHistoryManager {

    public static int getCallCount(Context context, String number) {

        int count = 0;

        Cursor cursor = context.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                null,
                CallLog.Calls.NUMBER + "=?",
                new String[]{number},
                null
        );

        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();
        }

        return count;
    }
}
