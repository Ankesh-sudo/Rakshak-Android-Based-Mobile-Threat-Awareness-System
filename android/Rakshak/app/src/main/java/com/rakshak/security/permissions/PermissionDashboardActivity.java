package com.rakshak.security.permissions;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.rakshak.security.R;

import java.util.ArrayList;
import java.util.List;

public class PermissionDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SimpleAppAdapter adapter;
    private PackageManager pm;

    // 🔐 Master list (never modified)
    private final List<ApplicationInfo> allApps = new ArrayList<>();

    // 🔘 Toggle state
    private boolean showSystemApps = false;

    // 🔍 Permission filter types
    private enum PermissionFilter {
        ALL,
        CAMERA,
        MICROPHONE,
        LOCATION,
        SMS
    }

    private PermissionFilter currentFilter = PermissionFilter.ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_dashboard);

        pm = getPackageManager();

        recyclerView = findViewById(R.id.permissionRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 🔹 Load installed apps ONCE
        allApps.addAll(pm.getInstalledApplications(PackageManager.GET_META_DATA));

        // 🔹 Adapter starts with EMPTY list
        adapter = new SimpleAppAdapter(new ArrayList<>(), pm);
        recyclerView.setAdapter(adapter);

        setupFilters();
        setupSystemAppToggle();

        // 🔹 First render
        applyCombinedFilter();
    }

    // ===============================
    // 🔘 SYSTEM APP TOGGLE
    // ===============================
    private void setupSystemAppToggle() {
        SwitchMaterial systemAppSwitch = findViewById(R.id.switchShowSystemApps);
        if (systemAppSwitch == null) return;

        systemAppSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showSystemApps = isChecked;
            applyCombinedFilter();
        });
    }

    // ===============================
    // 🔍 PERMISSION FILTER CHIPS
    // ===============================
    private void setupFilters() {

        Chip chipAll = findViewById(R.id.chipAll);
        Chip chipCamera = findViewById(R.id.chipCamera);
        Chip chipMic = findViewById(R.id.chipMic);
        Chip chipLocation = findViewById(R.id.chipLocation);
        Chip chipSMS = findViewById(R.id.chipSMS);

        if (chipAll != null) {
            chipAll.setOnClickListener(v -> {
                currentFilter = PermissionFilter.ALL;
                applyCombinedFilter();
            });
        }

        if (chipCamera != null) {
            chipCamera.setOnClickListener(v -> {
                currentFilter = PermissionFilter.CAMERA;
                applyCombinedFilter();
            });
        }

        if (chipMic != null) {
            chipMic.setOnClickListener(v -> {
                currentFilter = PermissionFilter.MICROPHONE;
                applyCombinedFilter();
            });
        }

        if (chipLocation != null) {
            chipLocation.setOnClickListener(v -> {
                currentFilter = PermissionFilter.LOCATION;
                applyCombinedFilter();
            });
        }

        if (chipSMS != null) {
            chipSMS.setOnClickListener(v -> {
                currentFilter = PermissionFilter.SMS;
                applyCombinedFilter();
            });
        }
    }

    // ===============================
    // 🧠 COMBINED FILTER ENGINE
    // ===============================
    private void applyCombinedFilter() {

        List<ApplicationInfo> result = new ArrayList<>();

        for (ApplicationInfo app : allApps) {

            boolean isSystemApp =
                    (app.flags & ApplicationInfo.FLAG_SYSTEM) != 0;

            // 🔘 System app toggle
            if (!showSystemApps && isSystemApp) {
                continue;
            }

            // 🔍 ALL → no permission scan
            if (currentFilter == PermissionFilter.ALL) {
                result.add(app);
                continue;
            }

            // 🔐 Permission scan
            List<String> permissions =
                    PermissionAnalyzer.analyze(pm, app.packageName);

            if (permissions == null || permissions.isEmpty()) continue;

            if (matchesPermissionFilter(permissions)) {
                result.add(app);
            }
        }

        // ✅ THIS is the critical line
        adapter.updateList(result);
    }

    // ===============================
    // 🧩 PERMISSION MATCH LOGIC
    // ===============================
    private boolean matchesPermissionFilter(List<String> permissions) {

        for (String p : permissions) {

            switch (currentFilter) {

                case CAMERA:
                    if (p.contains("CAMERA")) return true;
                    break;

                case MICROPHONE:
                    if (p.contains("RECORD_AUDIO")) return true;
                    break;

                case LOCATION:
                    if (p.contains("ACCESS_FINE_LOCATION")
                            || p.contains("ACCESS_COARSE_LOCATION")) {
                        return true;
                    }
                    break;

                case SMS:
                    if (p.contains("READ_SMS")
                            || p.contains("SEND_SMS")
                            || p.contains("RECEIVE_SMS")) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }
}
