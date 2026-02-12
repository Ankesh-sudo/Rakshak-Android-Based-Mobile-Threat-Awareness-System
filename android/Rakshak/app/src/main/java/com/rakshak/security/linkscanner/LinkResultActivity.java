package com.rakshak.security.linkscanner;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;

import com.rakshak.security.R;

public class LinkResultActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link_result);

        TextView title = findViewById(R.id.riskTitle);
        TextView urlView = findViewById(R.id.urlText);
        TextView reasonsView = findViewById(R.id.reasonText);
        TextView metaView = findViewById(R.id.metaText);
        Button openBtn = findViewById(R.id.openButton);
        Button cancelBtn = findViewById(R.id.cancelButton);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        // ================= RECEIVE DATA =================
        String originalUrl = intent.getStringExtra("original_url");
        String finalUrl = intent.getStringExtra("final_url");
        String level = intent.getStringExtra("risk_level");
        String reasons = intent.getStringExtra("risk_reasons");

        int riskScore = intent.getIntExtra("risk_score", 0);
        float confidence = intent.getFloatExtra("confidence", 0f);
        double entropy = intent.getDoubleExtra("entropy", 0.0);
        boolean isShortened = intent.getBooleanExtra("is_shortened", false);
        boolean isRedirected = intent.getBooleanExtra("is_redirected", false);

        if (TextUtils.isEmpty(finalUrl)) {
            finish();
            return;
        }

        // Clamp score to 100
        if (riskScore > 100) riskScore = 100;

        // ================= URL DISPLAY =================
        if (!TextUtils.isEmpty(originalUrl)
                && !originalUrl.equalsIgnoreCase(finalUrl)) {

            urlView.setText(
                    "Shared link:\n" + originalUrl +
                            "\n\nActual destination:\n" + finalUrl
            );
        } else {
            urlView.setText(finalUrl);
        }

        // ================= REASONS =================
        reasonsView.setText(
                !TextUtils.isEmpty(reasons)
                        ? reasons
                        : "No obvious risk indicators detected."
        );

        // ================= META INFO =================
        StringBuilder meta = new StringBuilder();
        meta.append("Risk score: ").append(riskScore).append("/100\n");
        meta.append("Confidence: ")
                .append(String.format("%.0f", confidence * 100))
                .append("%\n");

        if (entropy > 0) {
            meta.append("Entropy: ")
                    .append(String.format("%.2f", entropy))
                    .append("\n");
        }

        if (isShortened) {
            meta.append("Shortened link detected\n");
        }

        if (isRedirected) {
            meta.append("Redirected to another domain\n");
        }

        if (metaView != null) {
            metaView.setText(meta.toString().trim());
        }

        // ================= UI BASED ON RISK =================
        if ("HIGH".equals(level)) {

            title.setText("🚨 Dangerous Link");
            title.setTextColor(Color.RED);
            openBtn.setText("OPEN ANYWAY");

        } else if ("MEDIUM".equals(level)) {

            title.setText("⚠ Suspicious Link");
            title.setTextColor(Color.rgb(255, 165, 0));
            openBtn.setText("OPEN ANYWAY");

        } else {

            title.setText("✅ Safe Link");
            title.setTextColor(Color.GREEN);
            openBtn.setText("OPEN LINK");
        }

        // ================= USER ACTION =================
        openBtn.setOnClickListener(v -> {
            openInBrowser(finalUrl);
            finish();
        });

        cancelBtn.setOnClickListener(v -> finish());
    }

    private void openInBrowser(String url) {
        try {
            Intent browserIntent =
                    new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            browserIntent.addCategory(Intent.CATEGORY_BROWSABLE);
            startActivity(browserIntent);
        } catch (Exception ignored) {
        }
    }
}
