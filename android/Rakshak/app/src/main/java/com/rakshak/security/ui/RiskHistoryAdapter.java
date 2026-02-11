package com.rakshak.security.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.rakshak.security.R;
import com.rakshak.security.core.database.RiskDatabase;
import com.rakshak.security.core.database.RiskEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RiskHistoryAdapter
        extends RecyclerView.Adapter<RiskHistoryAdapter.ViewHolder> {

    private final List<RiskEntity> list;

    public RiskHistoryAdapter(List<RiskEntity> list) {
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_risk_history, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        RiskEntity entity = list.get(position);

        holder.typeText.setText(entity.type + " - " + entity.level);
        holder.sourceText.setText("Source: " + entity.source);
        holder.scoreText.setText("Score: " + entity.score);

        String formattedDate =
                new SimpleDateFormat("dd MMM yyyy HH:mm",
                        Locale.getDefault())
                        .format(new Date(entity.timestamp));

        holder.timeText.setText(formattedDate);

        // ===============================
        // Click to View + Delete
        // ===============================
        holder.itemView.setOnClickListener(v -> {

            Context context = v.getContext();

            String messageContent = entity.message != null
                    ? entity.message
                    : "No message stored.";

            new AlertDialog.Builder(context)
                    .setTitle("Risk Details")
                    .setMessage(
                            "Type: " + entity.type +
                                    "\nSource: " + entity.source +
                                    "\nScore: " + entity.score +
                                    "\nLevel: " + entity.level +
                                    "\n\nMessage:\n" + messageContent
                    )
                    .setPositiveButton("Delete", (dialog, which) -> {

                        new Thread(() -> {

                            RiskDatabase.getInstance(context)
                                    .riskDao()
                                    .delete(entity);

                            // Remove from list & update UI
                            ((android.app.Activity) context)
                                    .runOnUiThread(() -> {
                                        int adapterPosition = holder.getAdapterPosition();
                                        if (adapterPosition != RecyclerView.NO_POSITION) {
                                            list.remove(adapterPosition);
                                            notifyItemRemoved(adapterPosition);
                                            Toast.makeText(context,
                                                    "Deleted",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }).start();
                    })
                    .setNegativeButton("Close", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView typeText, sourceText, scoreText, timeText;

        public ViewHolder(View itemView) {
            super(itemView);

            typeText = itemView.findViewById(R.id.typeText);
            sourceText = itemView.findViewById(R.id.sourceText);
            scoreText = itemView.findViewById(R.id.scoreText);
            timeText = itemView.findViewById(R.id.timeText);
        }
    }
}
