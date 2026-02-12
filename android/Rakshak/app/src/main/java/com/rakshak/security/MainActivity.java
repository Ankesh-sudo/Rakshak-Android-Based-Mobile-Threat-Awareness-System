package com.rakshak.security;

import android.Manifest;
import android.app.role.RoleManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.rakshak.security.chatbot.ChatActivity;
import com.rakshak.security.emergency.EmergencyActivity;
import com.rakshak.security.filescanner.FilePickerHelper;
import com.rakshak.security.filescanner.FolderScanResultActivity;
import com.rakshak.security.health.HealthDashboardActivity;
import com.rakshak.security.linkscanner.LinkDashboardActivity;
import com.rakshak.security.permissions.PermissionDashboardActivity;
import com.rakshak.security.ui.RiskHistoryActivity;
import com.rakshak.security.ui.SecurityDashboardActivity;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_FOLDER_PICK = 2001;
    private static final int REQ_PHONE = 1001;
    private static final int REQ_CONTACTS = 1002;
    private static final int REQ_CALL_LOG = 1003;
    private static final int REQ_OVERLAY = 1004;
    private static final int REQ_ROLE = 1005;
    private static final int REQ_NOTIFICATION = 1006;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        applyEdgeToEdgeInsets();
        requestNotificationPermissionIfNeeded();
        setupDashboardButtons();
    }

    private void applyEdgeToEdgeInsets() {
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(
                    mainView,
                    (v, insets) -> {
                        Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                        v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                        return insets;
                    }
            );
        }
    }

    private void setupDashboardButtons() {

        setClick(R.id.btnCallProtection, v -> startCallProtectionFlow());

        setClick(R.id.btnLinkScanner, v ->
                startActivity(new Intent(this, LinkDashboardActivity.class)));

        setClick(R.id.btnScanFile, v ->
                FilePickerHelper.pickFile(this));

        // ✅ FIXED Scan Folder
        setClick(R.id.btnScanFolder, v -> openFolderPicker());

        setClick(R.id.btnPermissionTracker, v ->
                startActivity(new Intent(this, PermissionDashboardActivity.class)));

        setClick(R.id.btnHealthCheck, v ->
                startActivity(new Intent(this, HealthDashboardActivity.class)));

        setClick(R.id.btnChatBot, v ->
                startActivity(new Intent(this, ChatActivity.class)));

        setClick(R.id.btnSecurityDashboard, v ->
                startActivity(new Intent(this, SecurityDashboardActivity.class)));

        setClick(R.id.btnRiskHistory, v ->
                startActivity(new Intent(this, RiskHistoryActivity.class)));

        setClick(R.id.btnEmergency, v ->
                startActivity(new Intent(this, EmergencyActivity.class)));
    }

    private void setClick(int id, View.OnClickListener listener) {
        View view = findViewById(id);
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    // =================================================
    // FOLDER PICKER
    // =================================================

    private void openFolderPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQ_FOLDER_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_FOLDER_PICK && resultCode == RESULT_OK && data != null) {

            Uri folderUri = data.getData();

            // 🔥 Simulated Scan Result (Replace later with real scan)
            String scanResult = "SAFE - No malicious files found in selected folder";

            Intent intent = new Intent(this, FolderScanResultActivity.class);
            intent.putExtra("result", scanResult);
            intent.putExtra("folderUri", folderUri.toString());
            startActivity(intent);
        }
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

        if (!hasCallLogPermission()) {
            requestCallLogPermission();
            return;
        }

        if (!hasOverlayPermission()) {
            requestOverlayPermission();
            return;
        }

        requestCallScreeningRole();

        Toast.makeText(this,
                "Rakshak Call Protection Activated 🛡",
                Toast.LENGTH_SHORT).show();
    }

    private void requestCallScreeningRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            RoleManager roleManager =
                    (RoleManager) getSystemService(ROLE_SERVICE);

            if (roleManager != null &&
                    roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) &&
                    !roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {

                Intent intent =
                        roleManager.createRequestRoleIntent(
                                RoleManager.ROLE_CALL_SCREENING);

                startActivityForResult(intent, REQ_ROLE);
            }
        }
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQ_NOTIFICATION
                );
            }
        }
    }

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

    private boolean hasCallLogPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasOverlayPermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || Settings.canDrawOverlays(this);
    }

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

    private void requestCallLogPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.READ_CALL_LOG},
                REQ_CALL_LOG
        );
    }

    private void requestOverlayPermission() {
        Toast.makeText(this,
                "Overlay permission required for real-time call warnings",
                Toast.LENGTH_LONG).show();

        Intent intent = new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName())
        );

        startActivityForResult(intent, REQ_OVERLAY);
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

        if (requestCode == REQ_NOTIFICATION) return;

        if (grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCallProtectionFlow();
        } else {
            Toast.makeText(this,
                    "Permission denied. Rakshak protection incomplete.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
