package com.rakshak.security.permissions;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rakshak.security.R;

import java.util.ArrayList;
import java.util.List;

public class SimpleAppAdapter
        extends RecyclerView.Adapter<SimpleAppAdapter.ViewHolder> {

    private List<ApplicationInfo> apps;
    private final PackageManager pm;

    public SimpleAppAdapter(List<ApplicationInfo> apps, PackageManager pm) {
        this.apps = apps;
        this.pm = pm;
    }

    // ✅ REQUIRED for permission filtering
    public void updateList(List<ApplicationInfo> newApps) {
        this.apps = newApps;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_permission_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        ApplicationInfo app = apps.get(position);

        // ===============================
        // 🧱 BASIC APP INFO (SAFE)
        // ===============================
        try {
            holder.appName.setText(pm.getApplicationLabel(app));
            holder.packageName.setText(app.packageName);

            Drawable icon = pm.getApplicationIcon(app);
            holder.appIcon.setImageDrawable(icon);
        } catch (Exception e) {
            holder.appName.setText("Unknown App");
            holder.packageName.setText(app.packageName);
            holder.appIcon.setImageResource(android.R.drawable.sym_def_app_icon);
        }

        // ===============================
        // 🏷️ SYSTEM APP LABEL
        // ===============================
        boolean isSystemApp =
                (app.flags & ApplicationInfo.FLAG_SYSTEM) != 0;

        if (isSystemApp) {
            holder.systemAppLabel.setVisibility(View.VISIBLE);
            holder.systemAppLabel.setText(R.string.system_app_label);
        } else {
            holder.systemAppLabel.setVisibility(View.GONE);
        }

        // ===============================
        // 🔐 PERMISSION ANALYSIS
        // ===============================
        List<String> dangerousPermissions =
                PermissionAnalyzer.analyze(pm, app.packageName);

        if (dangerousPermissions == null || dangerousPermissions.isEmpty()) {
            holder.permissionSummary.setText(
                    holder.itemView.getContext()
                            .getString(R.string.no_dangerous_permissions)
            );
        } else {
            holder.permissionSummary.setText(
                    formatPermissions(dangerousPermissions)
            );
        }

        // ===============================
        // 🧠 RISK CLASSIFICATION
        // ===============================
        RiskLevel riskLevel =
                RiskClassifier.classify(dangerousPermissions);

        holder.riskLabel.setText(
                holder.itemView.getContext()
                        .getString(R.string.risk_label, riskLevel.name())
        );

        // ===============================
        // 🎨 RISK COLOR SIGNAL
        // ===============================
        int colorRes;
        switch (riskLevel) {
            case SAFE:
                colorRes = R.color.risk_safe;
                break;
            case CAUTION:
                colorRes = R.color.risk_caution;
                break;
            case HIGH:
            default:
                colorRes = R.color.risk_high;
                break;
        }

        holder.riskLabel.setTextColor(
                holder.itemView.getContext().getColor(colorRes)
        );

        // ===============================
        // 🟣 EDUCATION / DETAILS SCREEN
        // ===============================
        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(
                    v.getContext(),
                    PermissionEducationActivity.class
            );

            intent.putExtra(
                    PermissionEducationActivity.EXTRA_APP_NAME,
                    pm.getApplicationLabel(app).toString()
            );

            intent.putExtra(
                    PermissionEducationActivity.EXTRA_PACKAGE_NAME,
                    app.packageName
            );

            intent.putStringArrayListExtra(
                    PermissionEducationActivity.EXTRA_PERMISSIONS,
                    new ArrayList<>(dangerousPermissions)
            );

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return apps != null ? apps.size() : 0;
    }

    // ===============================
    // 🏷️ CLEAN PERMISSION FORMAT
    // ===============================
    private String formatPermissions(List<String> permissions) {
        StringBuilder builder = new StringBuilder();
        for (String permission : permissions) {
            builder.append("• ")
                    .append(permission.replace("android.permission.", ""))
                    .append("\n");
        }
        return builder.toString().trim();
    }

    // ===============================
    // 🔒 VIEW HOLDER
    // ===============================
    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView appIcon;
        TextView appName;
        TextView packageName;
        TextView permissionSummary;
        TextView riskLabel;
        TextView systemAppLabel;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            appIcon = itemView.findViewById(R.id.appIcon);
            appName = itemView.findViewById(R.id.appName);
            packageName = itemView.findViewById(R.id.packageName);
            permissionSummary = itemView.findViewById(R.id.permissionSummary);
            riskLabel = itemView.findViewById(R.id.riskLabel);
            systemAppLabel = itemView.findViewById(R.id.systemAppLabel);
        }
    }
}
