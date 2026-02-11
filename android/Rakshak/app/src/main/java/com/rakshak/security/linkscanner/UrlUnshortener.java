package com.rakshak.security.linkscanner;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class UrlUnshortener {

    private static final int MAX_REDIRECTS = 5;

    private UrlUnshortener() {
        // Utility class
    }

    /**
     * Resolves shortened URL and follows redirects
     */
    public static String resolveFinalUrl(String urlString) {
        if (urlString == null) return null;

        Set<String> visitedUrls = new HashSet<>();
        String currentUrl = urlString;

        try {
            for (int i = 0; i < MAX_REDIRECTS; i++) {

                if (visitedUrls.contains(currentUrl)) {
                    // Prevent redirect loops
                    break;
                }
                visitedUrls.add(currentUrl);

                URL url = new URL(currentUrl);
                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setInstanceFollowRedirects(false);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestMethod("HEAD");

                int responseCode = connection.getResponseCode();

                // Redirect codes
                if (responseCode >= 300 && responseCode < 400) {
                    String location = connection.getHeaderField("Location");
                    if (location == null) break;

                    // Handle relative redirects
                    URL nextUrl = new URL(url, location);
                    currentUrl = nextUrl.toString();
                } else {
                    // No redirect → final URL reached
                    break;
                }
            }
        } catch (Exception e) {
            // Fail-safe: return original URL
            return urlString;
        }

        return currentUrl;
    }

    /**
     * Detects common URL shorteners
     */
    public static boolean isShortenedUrl(String url) {
        if (url == null) return false;

        return url.matches(
                "https?://(bit\\.ly|t\\.co|tinyurl\\.com|goo\\.gl|ow\\.ly|is\\.gd|buff\\.ly|cutt\\.ly)/.*"
        );
    }
}
