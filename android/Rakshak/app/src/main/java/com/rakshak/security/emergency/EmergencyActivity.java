package com.rakshak.security.emergency;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.rakshak.security.R;

public class EmergencyActivity extends AppCompatActivity {

    private Button btnSOS;
    private View glowBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.fragment_emergency);

        applyEdgeToEdgeInsets();
        initializeViews();
        startPulseAnimation();
        setupSOSButton();
    }

    // =================================================
    // EDGE TO EDGE HANDLING
    // =================================================

    private void applyEdgeToEdgeInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(android.R.id.content),
                (v, insets) -> {
                    Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                    return insets;
                }
        );
    }

    // =================================================
    // INITIALIZE VIEWS
    // =================================================

    private void initializeViews() {

        btnSOS = findViewById(R.id.btnSOS);
        glowBackground = findViewById(R.id.glowBackground);
    }

    // =================================================
    // PULSE ANIMATION
    // =================================================

    private void startPulseAnimation() {

        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        glowBackground.startAnimation(pulse);
    }

    // =================================================
    // SOS BUTTON LOGIC
    // =================================================

    private void setupSOSButton() {

        Animation clickAnim = AnimationUtils.loadAnimation(this, R.anim.button_click);

        btnSOS.setOnClickListener(v -> {

            v.startAnimation(clickAnim);

            triggerEmergencyProtocol();
        });
    }

    // =================================================
    // EMERGENCY PROTOCOL
    // =================================================

    private void triggerEmergencyProtocol() {

        // 🔥 Visual feedback
        btnSOS.setText("ACTIVATED");
        btnSOS.setEnabled(false);

        // 🚨 TODO: Add real emergency logic here
        // Examples:
        // - Send emergency SMS
        // - Share live location
        // - Call trusted contact
        // - Alert backend server
        // - Trigger alarm sound

        Toast.makeText(
                this,
                "Emergency Protocol Activated 🚨",
                Toast.LENGTH_LONG
        ).show();
    }

    // =================================================
    // BACK BUTTON BEHAVIOR
    // =================================================

    @Override
    public void onBackPressed() {
        finish();
    }
}
