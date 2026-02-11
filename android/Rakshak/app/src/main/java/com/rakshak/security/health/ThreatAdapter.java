package com.rakshak.security.health;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.rakshak.security.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ThreatAdapter extends RecyclerView.Adapter<ThreatAdapter.ViewHolder> {

    private final ArrayList<MediaStoreScanner.ScanFile> threatList;
    private final Context context;

    public ThreatAdapter(Context context,
                         ArrayList<MediaStoreScanner.ScanFile> threatList) {
        this.context = context;
        this.threatList = threatList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_threat, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        MediaStoreScanner.ScanFile threat = threatList.get(position);

        holder.tvName.setText(threat.name);
        holder.tvPath.setText(threat.path);
        holder.tvReason.setText(threat.reason);
        holder.tvSize.setText("Size: " + formatSize(threat.size));

        applySeverityColor(holder, threat);

        // ================= DELETE BUTTON =================
        holder.btnDelete.setOnClickListener(v -> showDeleteDialog(threat, position));

        // ================= QUARANTINE BUTTON =================
        holder.btnQuarantine.setOnClickListener(v ->
                showQuarantineDialog(threat));
    }

    @Override
    public int getItemCount() {
        return threatList != null ? threatList.size() : 0;
    }

    // ==========================================================
    // VIEW HOLDER
    // ==========================================================

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvPath, tvReason, tvSize;
        Button btnDelete, btnQuarantine;

        public ViewHolder(View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvThreatName);
            tvPath = itemView.findViewById(R.id.tvThreatPath);
            tvReason = itemView.findViewById(R.id.tvThreatReason);
            tvSize = itemView.findViewById(R.id.tvThreatSize);

            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnQuarantine = itemView.findViewById(R.id.btnQuarantine);
        }
    }

    // ==========================================================
    // DELETE LOGIC (Play Store Safe - MediaStore Based)
    // ==========================================================

    private void showDeleteDialog(MediaStoreScanner.ScanFile threat, int position) {

        new AlertDialog.Builder(context)
                .setTitle("Delete File")
                .setMessage("Are you sure you want to delete this file?")
                .setPositiveButton("Delete", (dialog, which) -> {

                    try {
                        ContentResolver resolver = context.getContentResolver();
                        Uri uri = MediaStore.Files.getContentUri("external");

                        resolver.delete(
                                uri,
                                MediaStore.MediaColumns.DISPLAY_NAME + "=?",
                                new String[]{threat.name}
                        );

                        threatList.remove(position);
                        notifyItemRemoved(position);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ==========================================================
    // QUARANTINE (Currently Demo Placeholder)
    // ==========================================================

    private void showQuarantineDialog(MediaStoreScanner.ScanFile threat) {

        new AlertDialog.Builder(context)
                .setTitle("Quarantine File")
                .setMessage("Move this file to secure quarantine storage?")
                .setPositiveButton("Quarantine", (dialog, which) -> {
                    // TODO: Implement real quarantine logic later
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ==========================================================
    // SEVERITY COLOR
    // ==========================================================

    private void applySeverityColor(ViewHolder holder,
                                    MediaStoreScanner.ScanFile threat) {

        int color;

        String reason = threat.reason.toLowerCase();

        if (reason.contains("extension") ||
                reason.contains("double")) {

            color = ContextCompat.getColor(
                    holder.itemView.getContext(),
                    android.R.color.holo_red_light
            );

        } else if (reason.contains("apk")) {

            color = ContextCompat.getColor(
                    holder.itemView.getContext(),
                    android.R.color.holo_orange_light
            );

        } else {

            color = ContextCompat.getColor(
                    holder.itemView.getContext(),
                    android.R.color.holo_blue_light
            );
        }

        holder.tvReason.setTextColor(color);
    }

    // ==========================================================
    // SIZE FORMAT
    // ==========================================================

    private String formatSize(long size) {

        double mb = size / (1024.0 * 1024.0);

        if (mb < 1) {
            double kb = size / 1024.0;
            return new DecimalFormat("#.##").format(kb) + " KB";
        }

        return new DecimalFormat("#.##").format(mb) + " MB";
    }
}
