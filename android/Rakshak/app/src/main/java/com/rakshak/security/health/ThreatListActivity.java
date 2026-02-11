package com.rakshak.security.health;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rakshak.security.R;

import java.util.ArrayList;

public class ThreatListActivity extends AppCompatActivity {

    public static final String EXTRA_THREATS = "extra_threats";

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private TextView tvTitle;

    private ArrayList<MediaStoreScanner.ScanFile> threatList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_threat_list);

        recyclerView = findViewById(R.id.recyclerThreats);
        tvEmpty = findViewById(R.id.tvEmptyState);
        tvTitle = findViewById(R.id.tvThreatTitle);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadThreats();
    }

    @SuppressWarnings("unchecked")
    private void loadThreats() {

        threatList = (ArrayList<MediaStoreScanner.ScanFile>)
                getIntent().getSerializableExtra(EXTRA_THREATS);

        if (threatList == null) {
            threatList = new ArrayList<>();
        }

        updateUI();
    }

    private void updateUI() {

        if (threatList.isEmpty()) {

            tvTitle.setText("Detected Threats");
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("No security threats detected 🎉");

            recyclerView.setVisibility(View.GONE);

        } else {

            tvTitle.setText("Detected Threats (" + threatList.size() + ")");

            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            ThreatAdapter adapter =
                    new ThreatAdapter(this, threatList);

            recyclerView.setAdapter(adapter);
        }
    }
}
