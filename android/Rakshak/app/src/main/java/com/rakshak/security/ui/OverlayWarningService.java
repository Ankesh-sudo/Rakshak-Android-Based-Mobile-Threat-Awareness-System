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
import android.widget.TextView;

import com.rakshak.security.R;

public class OverlayWarningService extends Service {

    private WindowManager windowManager;
    private View overlayView;

    // 🔒 Prevent duplicate overlays
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
        TextView details = overlayView.findViewById(R.id.riskDetails);

        title.setText("⚠️ " + level + " RISK CALL");
        details.setText("Score: " + score + "\n" + reasons);

        // 🎨 Risk-based UI color
        if ("HIGH".equals(level)) {
            overlayView.setBackgroundColor(0xCCB71C1C); // 🔴 Red
        } else if ("MEDIUM".equals(level)) {
            overlayView.setBackgroundColor(0xCCF57C00); // 🟠 Orange
        } else {
            overlayView.setBackgroundColor(0xCC2E7D32); // 🟢 Green
        }

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

        // ⏱ Auto-dismiss overlay after 10 seconds (failsafe)
        overlayView.postDelayed(this::stopSelf, 10_000);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (overlayView != null && windowManager != null) {
            windowManager.removeView(overlayView);
            overlayView = null;
        }

        isOverlayShown = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
