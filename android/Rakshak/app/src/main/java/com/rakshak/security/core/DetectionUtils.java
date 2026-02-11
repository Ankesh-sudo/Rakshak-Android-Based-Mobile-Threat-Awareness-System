package com.rakshak.security.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DetectionUtils {

    public static boolean containsSuspiciousKeywords(String text) {

        String[] keywords = {
                "urgent", "bank", "verify",
                "lottery", "winner", "click here",
                "account blocked"
        };

        for (String word : keywords) {
            if (text.toLowerCase().contains(word)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsLink(String text) {

        Pattern urlPattern = Pattern.compile(
                "(https?:\\/\\/)?([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?"
        );

        Matcher matcher = urlPattern.matcher(text);
        return matcher.find();
    }
}
