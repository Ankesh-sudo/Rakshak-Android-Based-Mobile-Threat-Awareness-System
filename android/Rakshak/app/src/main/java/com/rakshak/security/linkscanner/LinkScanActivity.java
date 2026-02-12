package com.rakshak.security.linkscanner;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import com.rakshak.security.core.ml.CloudLinkClassifier;

import java.util.regex.Matcher;

public class LinkScanActivity extends Activity {

    private static final String TAG = "LINK_SCAN";

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

        Log.d(TAG, "Scanning URL: " + extractedUrl);

        // ================= STEP 1 — HEURISTIC =================
        LinkRiskResult heuristicResult =
                LinkRiskEngine.analyze(extractedUrl);

        int baseScore = heuristicResult.getRiskScore();

        // ================= STEP 2 — FEATURE EXTRACTION =================
        int urlLength = extractedUrl.length();
        int nDots = count(extractedUrl, '.');
        int nHyphens = count(extractedUrl, '-');
        int nUnderline = count(extractedUrl, '_');
        int nSlash = count(extractedUrl, '/');
        int nQuestion = count(extractedUrl, '?');
        int nEqual = count(extractedUrl, '=');
        int nAt = count(extractedUrl, '@');
        int nAnd = count(extractedUrl, '&');
        int nExclamation = count(extractedUrl, '!');
        int nSpace = count(extractedUrl, ' ');
        int nTilde = count(extractedUrl, '~');
        int nComma = count(extractedUrl, ',');
        int nPlus = count(extractedUrl, '+');
        int nAsterisk = count(extractedUrl, '*');
        int nHashtag = count(extractedUrl, '#');
        int nDollar = count(extractedUrl, '$');
        int nPercent = count(extractedUrl, '%');

        // Count actual redirection patterns like http:// inside URL
        int nRedirection = extractedUrl.split("http").length - 1;

        // ================= STEP 3 — ML BACKEND =================
        CloudLinkClassifier.predictLink(
                urlLength,
                nDots,
                nHyphens,
                nUnderline,
                nSlash,
                nQuestion,
                nEqual,
                nAt,
                nAnd,
                nExclamation,
                nSpace,
                nTilde,
                nComma,
                nPlus,
                nAsterisk,
                nHashtag,
                nDollar,
                nPercent,
                nRedirection,
                new CloudLinkClassifier.LinkPredictionCallback() {

                    @Override
                    public void onResult(float probability) {

                        Log.d(TAG, "ML Probability: " + probability);

                        int finalScore = mergeScores(baseScore, probability);

                        forwardResult(
                                extractedUrl,
                                heuristicResult,
                                finalScore,
                                probability
                        );
                    }

                    @Override
                    public void onError(String error) {

                        Log.e(TAG, "ML Error: " + error);

                        // Fallback → heuristic only
                        forwardResult(
                                extractedUrl,
                                heuristicResult,
                                baseScore,
                                0.5f // default neutral confidence
                        );
                    }
                }
        );
    }

    // ================= MERGE HEURISTIC + ML =================
    private int mergeScores(int baseScore, float probability) {

        int finalScore = baseScore;

        if (probability > 0.9f) {
            finalScore += 60;
        } else if (probability > 0.75f) {
            finalScore += 40;
        } else if (probability > 0.6f) {
            finalScore += 20;
        }

        return finalScore;
    }

    // ================= SEND RESULT =================
    private void forwardResult(String url,
                               LinkRiskResult heuristicResult,
                               int finalScore,
                               float mlProbability) {

        runOnUiThread(() -> {

            Intent resultIntent =
                    new Intent(this, LinkResultActivity.class);

            resultIntent.putExtra("original_url", url);
            resultIntent.putExtra("final_url",
                    heuristicResult.getFinalUrl());

            resultIntent.putExtra("risk_score", finalScore);

            // Better thresholds
            String level;

            if (finalScore >= 120) {
                level = "HIGH";
            } else if (finalScore >= 60) {
                level = "MEDIUM";
            } else {
                level = "SAFE";
            }

            resultIntent.putExtra("risk_level", level);

            resultIntent.putExtra("risk_reasons",
                    heuristicResult.getExplanationText());

            resultIntent.putExtra("confidence", mlProbability);

            resultIntent.putExtra("entropy",
                    heuristicResult.getEntropyScore());

            resultIntent.putExtra("is_shortened",
                    heuristicResult.isShortened());

            resultIntent.putExtra("is_redirected",
                    heuristicResult.isRedirected());

            resultIntent.putExtra("risk_summary",
                    heuristicResult.getSummary());

            startActivity(resultIntent);
            finish();
        });
    }

    // ================= URL RESOLUTION =================
    private String resolveIncomingUrl(Intent intent) {

        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action)
                && "text/plain".equals(type)) {

            String text = intent.getStringExtra(Intent.EXTRA_TEXT);

            if (TextUtils.isEmpty(text)) {
                text = intent.getStringExtra(Intent.EXTRA_SUBJECT);
            }

            if (TextUtils.isEmpty(text)
                    && intent.getClipData() != null) {
                text = extractFromClipData(intent.getClipData());
            }

            return normalizeUrl(extractUrl(text));
        }

        if (Intent.ACTION_PROCESS_TEXT.equals(action)) {

            CharSequence selectedText =
                    intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);

            return normalizeUrl(
                    extractUrl(selectedText != null
                            ? selectedText.toString()
                            : null)
            );
        }

        return normalizeUrl(intent.getStringExtra("url"));
    }

    private String extractUrl(String text) {
        if (TextUtils.isEmpty(text)) return null;

        Matcher matcher = Patterns.WEB_URL.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private String extractFromClipData(ClipData clipData) {
        if (clipData == null || clipData.getItemCount() == 0)
            return null;

        CharSequence cs = clipData.getItemAt(0).getText();
        return cs != null ? cs.toString() : null;
    }

    private String normalizeUrl(String url) {
        if (TextUtils.isEmpty(url)) return null;

        if (!url.startsWith("http://")
                && !url.startsWith("https://")) {
            return "https://" + url;
        }
        return url;
    }

    private int count(String text, char c) {
        int count = 0;
        for (char ch : text.toCharArray()) {
            if (ch == c) count++;
        }
        return count;
    }
}
