package com.rakshak.security.permissions;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.rakshak.security.R;

import java.util.ArrayList;

public class PermissionEducationActivity extends AppCompatActivity {

    public static final String EXTRA_APP_NAME = "app_name";
    public static final String EXTRA_PACKAGE_NAME = "package_name";
    public static final String EXTRA_PERMISSIONS = "permissions";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_education);

        ImageView appIcon = findViewById(R.id.eduAppIcon);
        TextView appNameView = findViewById(R.id.eduAppName);
        LinearLayout permissionContainer =
                findViewById(R.id.permissionExplanationContainer);
        Button openSettingsBtn = findViewById(R.id.openSettingsBtn);

        Intent intent = getIntent();

        String appName = intent.getStringExtra(EXTRA_APP_NAME);
        String packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME);
        ArrayList<String> permissions =
                intent.getStringArrayListExtra(EXTRA_PERMISSIONS);

        // ✅ Safe fallbacks
        if (appName == null || appName.trim().isEmpty()) {
            appName = "App Permissions";
        }

        if (packageName == null || packageName.trim().isEmpty()) {
            packageName = getPackageName();
        }

        appNameView.setText(appName);

        // ✅ Load app icon safely
        try {
            PackageManager pm = getPackageManager();
            Drawable icon = pm.getApplicationIcon(packageName);
            appIcon.setImageDrawable(icon);
        } catch (Exception e) {
            appIcon.setImageResource(R.mipmap.ic_launcher);
        }

        // ✅ Permission explanations
        if (permissions != null && !permissions.isEmpty()) {

            for (String permission : permissions) {
                TextView explanation = new TextView(this);

                explanation.setText(getExplanation(permission));
                explanation.setTextColor(
                        ContextCompat.getColor(this, R.color.text_secondary)
                );
                explanation.setTextSize(14f);
                explanation.setPadding(0, 12, 0, 12);

                permissionContainer.addView(explanation);
            }

        } else {
            TextView noData = new TextView(this);
            noData.setText("This app does not use any sensitive permissions.");
            noData.setTextColor(
                    ContextCompat.getColor(this, R.color.text_secondary)
            );
            noData.setTextSize(14f);
            noData.setPadding(0, 12, 0, 12);

            permissionContainer.addView(noData);
        }

        // ✅ Open system app permission settings
        final String finalPackageName = packageName;
        openSettingsBtn.setOnClickListener(v -> {
            Intent settingsIntent = new Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            );
            settingsIntent.setData(Uri.parse("package:" + finalPackageName));
            startActivity(settingsIntent);
        });
    }

    // 🧠 Human-friendly explanations (Stage 5 core)
    private String getExplanation(String permission) {

        switch (permission) {

            case "Camera":
                return "Camera access allows the app to take photos or record videos.\n\n"
                        + "✅ Normal for camera, video call, or scanner apps.\n"
                        + "⚠️ Risky if used by apps that don’t clearly need it.";

            case "Microphone":
                return "Microphone access allows the app to record audio.\n\n"
                        + "✅ Needed for calls, voice notes, or assistants.\n"
                        + "⚠️ Can be abused for eavesdropping if misused.";

            case "Location":
                return "Location access allows the app to know where you are.\n\n"
                        + "✅ Useful for maps, delivery, or weather apps.\n"
                        + "⚠️ Continuous access may track your movements.";

            case "SMS":
                return "SMS access allows reading or sending text messages.\n\n"
                        + "⚠️ High risk: can read OTPs and private messages.\n"
                        + "✅ Allow only for trusted messaging or banking apps.";

            case "Contacts":
                return "Contacts access allows reading your saved contacts.\n\n"
                        + "⚠️ Can expose personal relationships.\n"
                        + "✅ Acceptable for calling or contact-based apps.";

            default:
                return "This permission can affect your privacy.\n"
                        + "Only allow it if the app’s purpose is clear.";
        }
    }
}
