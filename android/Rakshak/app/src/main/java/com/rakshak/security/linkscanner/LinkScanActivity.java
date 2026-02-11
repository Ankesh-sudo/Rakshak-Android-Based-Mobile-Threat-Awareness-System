package com.rakshak.security.linkscanner;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import java.util.regex.Matcher;

public class LinkScanActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        // ================= RESOLVE URL =================
        String extractedUrl = resolveIncomingUrl(intent);

        if (TextUtils.isEmpty(extractedUrl)) {
            Toast.makeText(
                    this,
                    "No valid link found to scan",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
            return;
        }

        // ================= ANALYZE =================
        LinkRiskResult result = LinkRiskEngine.analyze(extractedUrl);

        // ================= FORWARD RESULT =================
        Intent resultIntent = new Intent(this, LinkResultActivity.class);

        // Core
        resultIntent.putExtra("original_url", result.getOriginalUrl());
        resultIntent.putExtra("final_url", result.getFinalUrl());
        resultIntent.putExtra("risk_level", result.level.name());
        resultIntent.putExtra("risk_score", result.getRiskScore());
        resultIntent.putExtra("risk_reasons", result.getExplanationText());

        // Advanced metadata (future-safe)
        resultIntent.putExtra("confidence", result.getConfidence());
        resultIntent.putExtra("entropy", result.getEntropyScore());
        resultIntent.putExtra("is_shortened", result.isShortened());
        resultIntent.putExtra("is_redirected", result.isRedirected());
        resultIntent.putExtra("risk_summary", result.getSummary());

        startActivity(resultIntent);
        finish();
    }

    /**
     * Resolves URL from:
     * 1) ACTION_SEND (Share)
     * 2) ACTION_PROCESS_TEXT (Selected text)
     * 3) ClipData (some apps)
     * 4) Internal app navigation
     */
    private String resolveIncomingUrl(Intent intent) {
        String action = intent.getAction();
        String type = intent.getType();

        // Case 1: Shared text
        if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {

            String text = intent.getStringExtra(Intent.EXTRA_TEXT);

            if (TextUtils.isEmpty(text)) {
                text = intent.getStringExtra(Intent.EXTRA_SUBJECT);
            }

            if (TextUtils.isEmpty(text) && intent.getClipData() != null) {
                text = extractFromClipData(intent.getClipData());
            }

            return normalizeUrl(extractUrl(text));
        }

        // Case 2: Selected text
        if (Intent.ACTION_PROCESS_TEXT.equals(action)) {
            CharSequence selectedText =
                    intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);

            return normalizeUrl(
                    extractUrl(selectedText != null ? selectedText.toString() : null)
            );
        }

        // Case 3: Internal navigation
        return normalizeUrl(intent.getStringExtra("url"));
    }

    /**
     * Extract first valid URL from text
     */
    private String extractUrl(String text) {
        if (TextUtils.isEmpty(text)) return null;

        Matcher matcher = Patterns.WEB_URL.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    /**
     * Extract text from ClipData
     */
    private String extractFromClipData(ClipData clipData) {
        if (clipData == null || clipData.getItemCount() == 0) return null;

        CharSequence cs = clipData.getItemAt(0).getText();
        return cs != null ? cs.toString() : null;
    }

    /**
     * Normalize URL scheme
     */
    private String normalizeUrl(String url) {
        if (TextUtils.isEmpty(url)) return null;

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "https://" + url;
        }
        return url;
    }
}
