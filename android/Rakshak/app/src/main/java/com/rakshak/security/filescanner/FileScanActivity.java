package com.rakshak.security.filescanner;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.rakshak.security.MainActivity;
import com.rakshak.security.R;

import java.util.ArrayList;

public class FileScanActivity extends AppCompatActivity {

    // ================= UI =================
    private TextView txtRiskLevel;
    private TextView txtRiskDescription;
    private TextView txtFilePath;
    private TextView txtFileMeta;
    private LinearLayout riskCard;
    private ProgressBar scanProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_scan);

        // ================= VIEW BINDING =================
        txtRiskLevel = findViewById(R.id.txtRiskLevel);
        txtRiskDescription = findViewById(R.id.txtRiskDescription);
        txtFilePath = findViewById(R.id.txtFilePath);
        txtFileMeta = findViewById(R.id.txtFileMeta);
        riskCard = findViewById(R.id.riskCard);
        scanProgress = findViewById(R.id.scanProgress);

        Button btnScanAnother = findViewById(R.id.btnScanAnother);
        Button btnBackHome = findViewById(R.id.btnBackHome);

        // ================= GET FILE (NORMAL + SHARE) =================
        Uri fileUri = extractIncomingFile();

        if (fileUri == null) {
            showErrorState();
            return;
        }

        // Persist permission if shared
        try {
            getContentResolver().takePersistableUriPermission(
                    fileUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
        } catch (Exception ignored) {}

        txtFilePath.setText(getFileNameSafe(fileUri));
        showFileMetadataSafe(fileUri);

        // ================= SCAN FILE (BACKGROUND THREAD) =================
        scanProgress.setVisibility(View.VISIBLE);

        new Thread(() -> {

            FileScanResult result;

            try {
                result = UniversalScanEngine.scan(
                        FileScanActivity.this,
                        fileUri
                );
            } catch (Exception e) {
                result = new FileScanResult(
                        FileScanResult.ThreatLevel.SUSPICIOUS,
                        "Scan failed. File may be unreadable."
                );
            }

            FileScanResult finalResult = result;

            runOnUiThread(() -> {
                scanProgress.setVisibility(View.GONE);
                updateUI(finalResult);

                // Save scan history (local)
                ScanHistoryManager.save(
                        FileScanActivity.this,
                        getFileNameSafe(fileUri),
                        finalResult.getThreatLevel().name()
                );
            });

        }).start();

        // ================= BUTTON ACTIONS =================
        btnScanAnother.setOnClickListener(v -> finish());

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_NEW_TASK
            );
            startActivity(intent);
            finish();
        });
    }

    // =================================================
    // HANDLE SHARE + NORMAL INTENTS
    // =================================================

    private Uri extractIncomingFile() {

        Intent intent = getIntent();

        if (intent == null) return null;

        // Single file share
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            return intent.getParcelableExtra(Intent.EXTRA_STREAM);
        }

        // Multiple file share (take first safely)
        if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction())) {
            ArrayList<Uri> uris =
                    intent.getParcelableArrayListExtra(
                            Intent.EXTRA_STREAM
                    );
            if (uris != null && !uris.isEmpty()) {
                return uris.get(0);
            }
        }

        // Normal in-app scan
        return intent.getData();
    }

    // =================================================
    // FILE METADATA (OEM SAFE)
    // =================================================

    private void showFileMetadataSafe(Uri uri) {

        long size = 0;

        try (Cursor cursor =
                     getContentResolver().query(
                             uri, null, null, null, null
                     )) {

            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex =
                        cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex != -1) {
                    size = cursor.getLong(sizeIndex);
                }
            }
        } catch (Exception ignored) {}

        String sizeText =
                size > 1024 * 1024
                        ? (size / (1024 * 1024)) + " MB"
                        : (size / 1024) + " KB";

        FileTypeDetector.FileType type =
                FileTypeDetector.detect(this, uri);

        txtFileMeta.setText(
                "Type: " + type.name() + " • Size: " + sizeText
        );
    }

    private String getFileNameSafe(Uri uri) {

        String name = "Shared file";

        try (Cursor cursor =
                     getContentResolver().query(
                             uri, null, null, null, null
                     )) {

            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex =
                        cursor.getColumnIndex(
                                OpenableColumns.DISPLAY_NAME
                        );
                if (nameIndex != -1) {
                    name = cursor.getString(nameIndex);
                }
            }
        } catch (Exception ignored) {}

        return name;
    }

    // =================================================
    // UI UPDATE BASED ON RISK
    // =================================================

    private void updateUI(FileScanResult result) {

        txtRiskLevel.setText(result.getThreatLevel().name());
        txtRiskDescription.setText(result.getDescription());

        switch (result.getThreatLevel()) {
            case SAFE:
                riskCard.setBackgroundResource(
                        R.drawable.bg_risk_safe
                );
                break;

            case SUSPICIOUS:
                riskCard.setBackgroundResource(
                        R.drawable.bg_risk_caution
                );
                break;

            case MALICIOUS:
                riskCard.setBackgroundResource(
                        R.drawable.bg_risk_high
                );
                break;
        }
    }

    // =================================================
    // ERROR STATE
    // =================================================

    private void showErrorState() {
        txtRiskLevel.setText("ERROR");
        txtRiskDescription.setText(
                "Unable to analyze the selected file."
        );
        riskCard.setBackgroundResource(
                R.drawable.bg_risk_caution
        );
    }
}
