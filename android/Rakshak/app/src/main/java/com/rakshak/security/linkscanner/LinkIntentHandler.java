package com.rakshak.security.linkscanner;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Patterns;

import java.util.regex.Matcher;

public class LinkIntentHandler {

    private LinkIntentHandler() {
        // Utility class (no instance)
    }

    /**
     * Extracts URL from incoming intent (Share or internal)
     */
    public static String extractUrlFromIntent(Intent intent) {
        if (intent == null) return null;

        String action = intent.getAction();
        String type = intent.getType();

        // Case 1: Shared from WhatsApp / browser
        if (Intent.ACTION_SEND.equals(action)
                && "text/plain".equals(type)) {

            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            return extractUrlFromText(sharedText);
        }

        // Case 2: Internal navigation
        return intent.getStringExtra("url");
    }

    /**
     * Extracts first valid URL from text
     */
    public static String extractUrlFromText(String text) {
        if (TextUtils.isEmpty(text)) return null;

        Matcher matcher = Patterns.WEB_URL.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
}
