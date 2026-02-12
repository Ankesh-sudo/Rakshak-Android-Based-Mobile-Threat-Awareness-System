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
import com.rakshak.security.core.ml.CloudFileClassifier;

import java.util.ArrayList;

public class FileScanActivity extends AppCompatActivity {

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

        txtRiskLevel = findViewById(R.id.txtRiskLevel);
        txtRiskDescription = findViewById(R.id.txtRiskDescription);
        txtFilePath = findViewById(R.id.txtFilePath);
        txtFileMeta = findViewById(R.id.txtFileMeta);
        riskCard = findViewById(R.id.riskCard);
        scanProgress = findViewById(R.id.scanProgress);

        Button btnScanAnother = findViewById(R.id.btnScanAnother);
        Button btnBackHome = findViewById(R.id.btnBackHome);

        final Uri fileUri = extractIncomingFile();

        if (fileUri == null) {
            showErrorState();
            return;
        }

        txtFilePath.setText(getFileNameSafe(fileUri));
        showFileMetadataSafe(fileUri);

        scanProgress.setVisibility(View.VISIBLE);

        new Thread(() -> {

            // ===============================
            // HEURISTIC SCAN
            // ===============================

            FileScanResult tempResult;

            try {
                tempResult = UniversalScanEngine.scan(
                        FileScanActivity.this,
                        fileUri
                );
            } catch (Exception e) {
                tempResult = new FileScanResult(
                        FileScanResult.ThreatLevel.SUSPICIOUS,
                        "Scan failed. File unreadable."
                );
            }

            final FileScanResult heuristicResult = tempResult;
            final int baseScore = convertThreatToScore(
                    heuristicResult.getThreatLevel()
            );

            // ===============================
            // FEATURE EXTRACTION FOR ML
            // ===============================

            int fileSize = getFileSizeSafe(fileUri);

            double entropy =
                    FileFeatureUtil.calculateEntropyFromUri(
                            FileScanActivity.this,
                            fileUri
                    );

            String fileName =
                    getFileNameSafe(fileUri).toLowerCase();

            int executable =
                    fileName.endsWith(".exe") ? 1 : 0;

            int suspiciousExt =
                    fileName.endsWith(".apk") ||
                            fileName.endsWith(".exe") ||
                            fileName.endsWith(".bat") ? 1 : 0;

            int hidden =
                    fileName.startsWith(".") ? 1 : 0;

            int doubleExtension =
                    fileName.split("\\.").length > 2 ? 1 : 0;

            int suspiciousName =
                    fileName.contains("crack") ||
                            fileName.contains("patch") ||
                            fileName.contains("keygen") ? 1 : 0;

            // ===============================
            // CALL ML BACKEND
            // ===============================

            CloudFileClassifier.predictFile(
                    fileSize,
                    entropy,
                    executable,
                    suspiciousExt,
                    hidden,
                    doubleExtension,
                    suspiciousName,
                    new CloudFileClassifier.FilePredictionCallback() {

                        @Override
                        public void onResult(float probability) {

                            int finalScore = baseScore;

                            if (probability > 0.9f) {
                                finalScore += 60;
                            } else if (probability > 0.75f) {
                                finalScore += 40;
                            } else if (probability > 0.6f) {
                                finalScore += 20;
                            }

                            updateFinalUI(
                                    finalScore,
                                    heuristicResult.getDescription()
                            );
                        }

                        @Override
                        public void onError(String error) {

                            updateFinalUI(
                                    baseScore,
                                    heuristicResult.getDescription()
                            );
                        }
                    }
            );

        }).start();

        btnScanAnother.setOnClickListener(v -> finish());

        btnBackHome.setOnClickListener(v -> {
            Intent intent =
                    new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    // =================================================
    // FINAL UI UPDATE
    // =================================================

    private void updateFinalUI(int finalScore,
                               String description) {

        runOnUiThread(() -> {

            scanProgress.setVisibility(View.GONE);

            FileScanResult.ThreatLevel level;

            if (finalScore >= 80) {
                level = FileScanResult.ThreatLevel.MALICIOUS;
            } else if (finalScore >= 50) {
                level = FileScanResult.ThreatLevel.SUSPICIOUS;
            } else {
                level = FileScanResult.ThreatLevel.SAFE;
            }

            txtRiskLevel.setText(level.name());
            txtRiskDescription.setText(description);

            switch (level) {
                case SAFE:
                    riskCard.setBackgroundResource(
                            R.drawable.bg_risk_safe);
                    break;

                case SUSPICIOUS:
                    riskCard.setBackgroundResource(
                            R.drawable.bg_risk_caution);
                    break;

                case MALICIOUS:
                    riskCard.setBackgroundResource(
                            R.drawable.bg_risk_high);
                    break;
            }
        });
    }

    // =================================================
    // UTIL METHODS
    // =================================================

    private int convertThreatToScore(
            FileScanResult.ThreatLevel level) {

        switch (level) {
            case MALICIOUS: return 80;
            case SUSPICIOUS: return 50;
            default: return 20;
        }
    }

    private int getFileSizeSafe(Uri uri) {

        long size = 0;

        try (Cursor cursor =
                     getContentResolver().query(
                             uri, null, null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex =
                        cursor.getColumnIndex(
                                OpenableColumns.SIZE);
                if (sizeIndex != -1) {
                    size = cursor.getLong(sizeIndex);
                }
            }
        } catch (Exception ignored) {}

        return (int) size;
    }

    private Uri extractIncomingFile() {

        Intent intent = getIntent();
        if (intent == null) return null;

        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            return intent.getParcelableExtra(
                    Intent.EXTRA_STREAM);
        }

        if (Intent.ACTION_SEND_MULTIPLE.equals(
                intent.getAction())) {

            ArrayList<Uri> uris =
                    intent.getParcelableArrayListExtra(
                            Intent.EXTRA_STREAM);

            if (uris != null && !uris.isEmpty()) {
                return uris.get(0);
            }
        }

        return intent.getData();
    }


    private void showFileMetadataSafe(Uri uri) {

        int size = getFileSizeSafe(uri);

        String sizeText =
                size > 1024 * 1024
                        ? (size / (1024 * 1024)) + " MB"
                        : (size / 1024) + " KB";

        txtFileMeta.setText("Size: " + sizeText);
    }

    private String getFileNameSafe(Uri uri) {

        String name = "Shared file";

        try (Cursor cursor =
                     getContentResolver().query(
                             uri, null, null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex =
                        cursor.getColumnIndex(
                                OpenableColumns.DISPLAY_NAME);

                if (nameIndex != -1) {
                    name = cursor.getString(nameIndex);
                }
            }
        } catch (Exception ignored) {}

        return name;
    }

    private void showErrorState() {
        txtRiskLevel.setText("ERROR");
        txtRiskDescription.setText(
                "Unable to analyze selected file.");
        riskCard.setBackgroundResource(
                R.drawable.bg_risk_caution);
    }
}
