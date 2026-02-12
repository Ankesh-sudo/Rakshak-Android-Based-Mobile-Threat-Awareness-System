package com.rakshak.security.linkscanner;

public class LinkFeatureExtractor {

    public static int count(String text, char c) {
        int count = 0;
        for (char ch : text.toCharArray()) {
            if (ch == c) count++;
        }
        return count;
    }

    public static int getUrlLength(String url) {
        return url.length();
    }

    public static int getRedirectionCount(String url) {
        return url.split("//", -1).length - 1;
    }
}
