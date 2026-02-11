package com.rakshak.security.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rakshak.security.R;
import com.rakshak.security.core.database.RiskDatabase;
import com.rakshak.security.core.database.RiskEntity;

import java.util.List;

public class RiskHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RiskHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_risk_history);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadData();
    }

    private void loadData() {

        new Thread(() -> {

            List<RiskEntity> list =
                    RiskDatabase.getInstance(this)
                            .riskDao()
                            .getAll();

            runOnUiThread(() -> {
                adapter = new RiskHistoryAdapter(list);
                recyclerView.setAdapter(adapter);
            });

        }).start();
    }
}
