package com.rakshak.security;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.rakshak.security.filescanner.FilePickerHelper;
import com.rakshak.security.filescanner.FileScanActivity;
import com.rakshak.security.filescanner.FolderScanResult;
import com.rakshak.security.filescanner.FolderScanResultActivity;
import com.rakshak.security.filescanner.FolderScanner;
import com.rakshak.security.linkscanner.LinkDashboardActivity;
import com.rakshak.security.permissions.PermissionDashboardActivity;
import com.rakshak.security.health.HealthDashboardActivity; // ✅ NEW IMPORT

public class MainActivity extends AppCompatActivity {

    // ================= REQUEST CODES =================
    private static final int REQ_PHONE = 1001;
    private static final int REQ_CONTACTS = 1002;
    private static final int REQ_OVERLAY = 1003;
    private static final int REQ_PICK_FOLDER = 2001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {
                    Insets bars =
                            insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(
                            bars.left,
                            bars.top,
                            bars.right,
                            bars.bottom
                    );
                    return insets;
                }
        );

        // ================= DASHBOARD BUTTONS =================

        findViewById(R.id.btnCallProtection)
                .setOnClickListener(v -> startCallProtectionFlow());

        findViewById(R.id.btnLinkScanner)
                .setOnClickListener(v ->
                        startActivity(
                                new Intent(
                                        MainActivity.this,
                                        LinkDashboardActivity.class
                                )
                        )
                );

        findViewById(R.id.btnScanFile)
                .setOnClickListener(v ->
                        FilePickerHelper.pickFile(MainActivity.this)
                );

        findViewById(R.id.btnScanFolder)
                .setOnClickListener(v -> openFolderPicker());

        findViewById(R.id.btnPermissionTracker)
                .setOnClickListener(v ->
                        startActivity(
                                new Intent(
                                        MainActivity.this,
                                        PermissionDashboardActivity.class
                                )
                        )
                );

        // ✅ UPDATED: OPEN HEALTH DASHBOARD
        findViewById(R.id.btnHealthCheck)
                .setOnClickListener(v ->
                        startActivity(
                                new Intent(
                                        MainActivity.this,
                                        HealthDashboardActivity.class
                                )
                        )
                );
    }

    // =================================================
    // FILE & FOLDER SCANNING
    // =================================================

    private void openFolderPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION |
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION |
                        Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
        );

        startActivityForResult(intent, REQ_PICK_FOLDER);
    }

    // =================================================
    // CALL PROTECTION FLOW
    // =================================================

    private void startCallProtectionFlow() {

        if (!hasPhonePermission()) {
            requestPhonePermission();
            return;
        }

        if (!hasContactPermission()) {
            requestContactPermission();
            return;
        }

        if (!hasOverlayPermission()) {
            requestOverlayPermission();
            return;
        }

        Toast.makeText(
                this,
                "Call protection is active",
                Toast.LENGTH_SHORT
        ).show();
    }

    // ================= PERMISSION CHECKS =================

    private boolean hasPhonePermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasContactPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasOverlayPermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || Settings.canDrawOverlays(this);
    }

    // ================= PERMISSION REQUESTS =================

    private void requestPhonePermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.READ_PHONE_STATE},
                REQ_PHONE
        );
    }

    private void requestContactPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.READ_CONTACTS},
                REQ_CONTACTS
        );
    }

    private void requestOverlayPermission() {

        Toast.makeText(
                this,
                "Overlay permission is required to show call warnings",
                Toast.LENGTH_LONG
        ).show();

        Intent intent = new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName())
        );
        startActivityForResult(intent, REQ_OVERLAY);
    }

    // =================================================
    // ACTIVITY RESULTS
    // =================================================

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_OVERLAY) {
            if (hasOverlayPermission()) {
                Toast.makeText(
                        this,
                        "Overlay permission enabled",
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                showPermissionWarning(
                        "Without overlay permission, Rakshak cannot warn you during calls"
                );
            }
        }

        if (requestCode == FilePickerHelper.PICK_FILE_REQUEST
                && resultCode == RESULT_OK
                && data != null) {

            Uri fileUri = data.getData();
            if (fileUri == null) return;

            Intent intent =
                    new Intent(this, FileScanActivity.class);
            intent.setData(fileUri);
            startActivity(intent);
        }

        if (requestCode == REQ_PICK_FOLDER
                && resultCode == RESULT_OK
                && data != null) {

            try {
                Uri folderUri = data.getData();
                if (folderUri == null) return;

                try {
                    getContentResolver().takePersistableUriPermission(
                            folderUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                } catch (Exception ignored) {}

                new Thread(() -> {

                    FolderScanResult result =
                            FolderScanner.scanFolder(
                                    MainActivity.this,
                                    folderUri
                            );

                    runOnUiThread(() -> {

                        Intent intent =
                                new Intent(
                                        MainActivity.this,
                                        FolderScanResultActivity.class
                                );

                        intent.putExtra(
                                "result",
                                result.getThreatLevel()
                                        + " • Files scanned: "
                                        + result.getTotalFiles()
                                        + "\n"
                                        + result.getMessage()
                        );

                        startActivity(intent);
                    });

                }).start();

            } catch (Exception e) {
                Toast.makeText(
                        this,
                        "Unable to scan this folder due to system restriction.",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == REQ_PHONE) {
            if (isGranted(grantResults)) {
                startCallProtectionFlow();
            } else {
                showPermissionWarning(
                        "Phone permission is required to detect incoming calls"
                );
            }
        } else if (requestCode == REQ_CONTACTS) {
            startCallProtectionFlow();
        }
    }

    // ================= HELPERS =================

    private boolean isGranted(int[] results) {
        return results.length > 0
                && results[0] == PackageManager.PERMISSION_GRANTED;
    }

    private void showPermissionWarning(String message) {
        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }
}
