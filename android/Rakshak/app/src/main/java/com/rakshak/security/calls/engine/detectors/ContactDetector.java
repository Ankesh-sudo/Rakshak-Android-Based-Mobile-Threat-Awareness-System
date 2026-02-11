package com.rakshak.security.calls.engine.detectors;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

public class ContactDetector {

    public static int analyze(Context context, String number) {

        Cursor cursor = context.getContentResolver().query(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI
                        .buildUpon()
                        .appendPath(number)
                        .build(),
                new String[]{ContactsContract.PhoneLookup._ID},
                null,
                null,
                null
        );

        boolean exists = false;

        if (cursor != null) {
            exists = cursor.moveToFirst();
            cursor.close();
        }

        // If NOT in contacts → add risk
        return exists ? 0 : 25;
    }
}
