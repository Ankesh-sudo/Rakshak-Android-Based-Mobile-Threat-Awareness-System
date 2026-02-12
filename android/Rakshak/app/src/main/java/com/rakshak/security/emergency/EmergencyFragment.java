package com.rakshak.security.emergency;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.rakshak.security.R;

public class EmergencyFragment extends Fragment {

    private Button btnSOS;
    private View glowBackground;

    public EmergencyFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_emergency, container, false);

        btnSOS = view.findViewById(R.id.btnSOS);
        glowBackground = view.findViewById(R.id.glowBackground);

        setupAnimations();
        setupEmergencyButton();

        return view;
    }

    private void setupAnimations() {

        // Pulse animation for glow
        Animation pulse = AnimationUtils.loadAnimation(requireContext(), R.anim.pulse);
        glowBackground.startAnimation(pulse);
    }

    private void setupEmergencyButton() {

        Animation clickAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.button_click);

        btnSOS.setOnClickListener(v -> {

            v.startAnimation(clickAnim);

            triggerEmergencyProtocol();
        });
    }

    private void triggerEmergencyProtocol() {

        // 🚨 TODO: Add your real emergency logic here
        // Example:
        // - Send SMS
        // - Alert backend
        // - Log high risk
        // - Activate siren sound

        // For now just change button text
        btnSOS.setText("ACTIVATED");
        btnSOS.setEnabled(false);
    }
}
