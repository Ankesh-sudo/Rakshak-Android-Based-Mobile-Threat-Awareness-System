package com.rakshak.security;

import android.Manifest;
import android.app.role.RoleManager;
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

import com.rakshak.security.chatbot.ChatActivity;
import com.rakshak.security.filescanner.FilePickerHelper;
import com.rakshak.security.health.HealthDashboardActivity;
import com.rakshak.security.linkscanner.LinkDashboardActivity;
import com.rakshak.security.permissions.PermissionDashboardActivity;

public class MainActivity extends AppCompatActivity {

    // ================= REQUEST CODES =================
    private static final int REQ_PHONE = 1001;
    private static final int REQ_CONTACTS = 1002;
    private static final int REQ_CALL_LOG = 1003;
    private static final int REQ_OVERLAY = 1004;
    private static final int REQ_ROLE = 1005;

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
                        startActivity(new Intent(this, LinkDashboardActivity.class)));

        findViewById(R.id.btnScanFile)
                .setOnClickListener(v ->
                        FilePickerHelper.pickFile(this));

        findViewById(R.id.btnPermissionTracker)
                .setOnClickListener(v ->
                        startActivity(new Intent(this, PermissionDashboardActivity.class)));

        findViewById(R.id.btnHealthCheck)
                .setOnClickListener(v ->
                        startActivity(new Intent(this, HealthDashboardActivity.class)));

        findViewById(R.id.btnChatBot)
                .setOnClickListener(v ->
                        startActivity(new Intent(this, ChatActivity.class)));
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
                "Call Protection Activated",
                Toast.LENGTH_SHORT).show();
    }

    // =================================================
    // ROLE REQUEST
    // =================================================

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

    // =================================================
    // PERMISSION CHECKS
    // =================================================

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

    // =================================================
    // PERMISSION REQUESTS
    // =================================================

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

        Toast.makeText(
                this,
                "Overlay permission required for call warnings",
                Toast.LENGTH_LONG
        ).show();

        Intent intent = new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName())
        );

        startActivityForResult(intent, REQ_OVERLAY);
    }

    // =================================================
    // PERMISSION RESULTS
    // =================================================

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

        if (isGranted(grantResults)) {
            startCallProtectionFlow();
        }
    }

    // =================================================
    // HELPER
    // =================================================

    private boolean isGranted(int[] results) {
        return results.length > 0
                && results[0] == PackageManager.PERMISSION_GRANTED;
    }
}
