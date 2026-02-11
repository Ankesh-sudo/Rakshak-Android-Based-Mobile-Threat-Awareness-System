package com.rakshak.security.health;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.rakshak.security.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ThreatAdapter extends RecyclerView.Adapter<ThreatAdapter.ViewHolder> {

    private final ArrayList<MediaStoreScanner.ScanFile> threatList;

    public ThreatAdapter(ArrayList<MediaStoreScanner.ScanFile> threatList) {
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
    }

    @Override
    public int getItemCount() {
        return threatList != null ? threatList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvPath, tvReason, tvSize;

        public ViewHolder(View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvThreatName);
            tvPath = itemView.findViewById(R.id.tvThreatPath);
            tvReason = itemView.findViewById(R.id.tvThreatReason);
            tvSize = itemView.findViewById(R.id.tvThreatSize);
        }
    }

    // ================= SEVERITY COLOR LOGIC =================

    private void applySeverityColor(ViewHolder holder,
                                    MediaStoreScanner.ScanFile threat) {

        int color;

        String reason = threat.reason.toLowerCase();

        if (reason.contains("executable") ||
                reason.contains("double extension")) {

            // 🔴 High Risk
            color = ContextCompat.getColor(
                    holder.itemView.getContext(),
                    android.R.color.holo_red_light
            );

        } else if (reason.contains("apk")) {

            // 🟠 Medium Risk
            color = ContextCompat.getColor(
                    holder.itemView.getContext(),
                    android.R.color.holo_orange_light
            );

        } else {

            // 🔵 Informational
            color = ContextCompat.getColor(
                    holder.itemView.getContext(),
                    android.R.color.holo_blue_light
            );
        }

        holder.tvReason.setTextColor(color);
    }

    // ================= SIZE FORMATTER =================

    private String formatSize(long size) {

        double mb = size / (1024.0 * 1024.0);

        if (mb < 1) {
            double kb = size / 1024.0;
            return new DecimalFormat("#.##").format(kb) + " KB";
        }

        return new DecimalFormat("#.##").format(mb) + " MB";
    }
}
