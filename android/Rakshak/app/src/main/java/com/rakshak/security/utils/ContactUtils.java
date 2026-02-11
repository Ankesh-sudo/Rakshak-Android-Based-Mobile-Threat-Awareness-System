package com.rakshak.security.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;

/**
 * Contact utility functions used for trust scoring.
 * Step 2.1 upgrade: normalization + metadata hooks.
 */
public class ContactUtils {

    /**
     * Simple check: is the number saved in contacts
     * (Backwards compatible with Step 1)
     */
    public static boolean isNumberInContacts(Context context, String number) {
        return getContactId(context, number) != -1;
    }

    /**
     * Returns contact ID if exists, else -1
     * Enables future trust scoring features.
     */
    public static long getContactId(Context context, String number) {

        if (context == null || TextUtils.isEmpty(number)) {
            return -1;
        }

        String normalizedNumber = normalizeNumber(number);

        Uri uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(normalizedNumber)
        );

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    uri,
                    new String[]{
                            ContactsContract.PhoneLookup._ID
                    },
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(0);
            }

        } catch (Exception e) {
            // Fail-safe: never crash security system
        } finally {
            if (cursor != null) cursor.close();
        }

        return -1;
    }

    /**
     * Normalizes phone numbers to reduce false mismatches.
     * Keeps last 10 digits (works well in India & many regions).
     */
    private static String normalizeNumber(String number) {

        if (TextUtils.isEmpty(number)) return number;

        // Remove spaces, dashes, brackets
        String cleaned = number.replaceAll("[^0-9+]", "");

        // Keep last 10 digits if longer (handles country codes)
        if (cleaned.length() > 10) {
            cleaned = cleaned.substring(cleaned.length() - 10);
        }

        return cleaned;
    }
}
