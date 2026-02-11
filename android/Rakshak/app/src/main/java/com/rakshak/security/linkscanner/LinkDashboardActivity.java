package com.rakshak.security.linkscanner;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.rakshak.security.R;

public class LinkDashboardActivity extends Activity {

    private EditText linkInput;
    private Button scanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link_dashboard);

        linkInput = findViewById(R.id.linkInput);
        scanButton = findViewById(R.id.scanButton);

        // 🔹 Auto-paste from clipboard (user-friendly, Play-safe)
        autoPasteFromClipboard();

        scanButton.setOnClickListener(v -> {
            scanButton.setEnabled(false); // prevent double taps

            String url = linkInput.getText().toString().trim();

            if (TextUtils.isEmpty(url)) {
                Toast.makeText(
                        this,
                        "Please paste a link to scan",
                        Toast.LENGTH_SHORT
                ).show();
                scanButton.setEnabled(true);
                return;
            }

            // Forward to LinkScanActivity (single scan pipeline)
            Intent intent = new Intent(
                    LinkDashboardActivity.this,
                    LinkScanActivity.class
            );
            intent.putExtra("url", url);
            startActivity(intent);

            scanButton.setEnabled(true);
        });
    }

    /**
     * Auto-fill link from clipboard if user has copied a URL
     * (User-initiated, no background access)
     */
    private void autoPasteFromClipboard() {
        ClipboardManager clipboard =
                (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        if (clipboard == null || !clipboard.hasPrimaryClip()) return;

        ClipData clipData = clipboard.getPrimaryClip();
        if (clipData == null || clipData.getItemCount() == 0) return;

        CharSequence clipText = clipData.getItemAt(0).getText();
        if (clipText == null) return;

        String text = clipText.toString().trim();

        if (looksLikeUrl(text)) {
            linkInput.setText(text);
            linkInput.setSelection(text.length());

            Toast.makeText(
                    this,
                    "Link detected from clipboard",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    /**
     * Lightweight URL hint check (not validation)
     */
    private boolean looksLikeUrl(String text) {
        return text.startsWith("http://")
                || text.startsWith("https://")
                || text.contains(".");
    }
}
