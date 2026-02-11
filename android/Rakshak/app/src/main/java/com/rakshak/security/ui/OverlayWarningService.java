package com.rakshak.security.ui;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.rakshak.security.R;

public class OverlayWarningService extends Service {

    private WindowManager windowManager;
    private View overlayView;

    private static boolean isOverlayShown = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (isOverlayShown) {
            return START_NOT_STICKY;
        }
        isOverlayShown = true;

        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        String level = intent.getStringExtra("risk_level");
        int score = intent.getIntExtra("risk_score", 0);
        String reasons = intent.getStringExtra("risk_reasons");
        String phoneNumber = intent.getStringExtra("phone_number");

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        if (windowManager == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        overlayView = inflater.inflate(R.layout.overlay_warning, null);

        if (overlayView == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        TextView title = overlayView.findViewById(R.id.riskTitle);
        TextView numberView = overlayView.findViewById(R.id.riskNumber);
        TextView scoreView = overlayView.findViewById(R.id.riskScore);
        TextView details = overlayView.findViewById(R.id.riskDetails);
        Button dismissBtn = overlayView.findViewById(R.id.btnDismiss);

        // -------- TEXT CONTENT --------
        title.setText("⚠ " + level + " RISK CALL");

        if (phoneNumber != null) {
            numberView.setText("Number: " + phoneNumber);
        } else {
            numberView.setText("Unknown Number");
        }

        scoreView.setText("Risk Score: " + score + "%");

        if (reasons != null) {
            details.setText(reasons);
        } else {
            details.setText("Suspicious activity detected.");
        }

        // -------- COLOR SYSTEM --------
        if ("HIGH".equals(level)) {
            overlayView.setBackgroundColor(0xCCB71C1C); // Red
        } else if ("MEDIUM".equals(level)) {
            overlayView.setBackgroundColor(0xCCF57C00); // Orange
        } else {
            overlayView.setBackgroundColor(0xCC2E7D32); // Green
        }

        // -------- DISMISS BUTTON --------
        dismissBtn.setOnClickListener(v -> stopSelf());

        // -------- WINDOW CONFIG --------
        int layoutFlag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP;

        windowManager.addView(overlayView, params);

        // -------- AUTO DISMISS FAILSAFE --------
        overlayView.postDelayed(this::stopSelf, 12_000);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (overlayView != null && windowManager != null) {
            try {
                windowManager.removeView(overlayView);
            } catch (Exception ignored) {}
            overlayView = null;
        }

        isOverlayShown = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
