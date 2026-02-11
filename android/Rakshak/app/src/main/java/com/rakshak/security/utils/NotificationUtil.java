package com.rakshak.security.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.rakshak.security.R;
import com.rakshak.security.core.RiskResult;
import com.rakshak.security.sms.SmsWarningActivity;
import com.rakshak.security.ui.SecurityDashboardActivity;

/**
 * Production-grade Notification Utility for Rakshak
 */
public final class NotificationUtil {

    private static final String CHANNEL_ID = "rakshak_security_alerts";
    private static final String CHANNEL_NAME = "Rakshak Security Alerts";
    private static final String GROUP_SMS = "rakshak_sms_group";
    private static final int SUMMARY_ID = 9999;

    private NotificationUtil() {}

    // =================================================
    // PUBLIC API
    // =================================================

    public static void showSmsWarning(
            Context context,
            String sender,
            String message,
            RiskResult result
    ) {

        if (context == null || result == null) return;

        if (!hasNotificationPermission(context)) return;

        String title = "⚠ Suspicious SMS Detected";
        String subtitle = (sender != null && !sender.isEmpty())
                ? "From: " + sender
                : "Unknown sender";

        showNotification(context, title, subtitle, sender, message, result);
    }

    // =================================================
    // CORE NOTIFICATION LOGIC
    // =================================================

    private static void showNotification(
            Context context,
            String title,
            String subtitle,
            String sender,
            String message,
            RiskResult result
    ) {

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager == null) return;

        createChannelIfNeeded(manager, result);

        int color = getRiskColor(result.getRiskLevel());
        int notificationId = generateNotificationId();

        // 🔥 Tap → Open SMS Warning Screen
        Intent tapIntent = new Intent(context, SmsWarningActivity.class);
        tapIntent.putExtra("sender", sender);
        tapIntent.putExtra("message", message);
        tapIntent.putExtra("score", result.getScore());
        tapIntent.putExtra("level", result.getRiskLevel().name());
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent tapPendingIntent = PendingIntent.getActivity(
                context,
                notificationId, // unique request code
                tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 🔥 Action Button → Open Dashboard
        Intent dashboardIntent =
                new Intent(context, SecurityDashboardActivity.class);
        dashboardIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent dashboardPendingIntent = PendingIntent.getActivity(
                context,
                notificationId + 1,
                dashboardIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(getRiskIcon(result.getRiskLevel()))
                        .setContentTitle(title)
                        .setContentText(subtitle)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(result.getExplanationText()))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setColor(color)
                        .setAutoCancel(true)
                        .setContentIntent(tapPendingIntent)
                        .addAction(
                                android.R.drawable.ic_menu_view,
                                "Open Dashboard",
                                dashboardPendingIntent
                        )
                        .setGroup(GROUP_SMS);

        manager.notify(notificationId, builder.build());

        showGroupSummary(context, manager);
    }

    // =================================================
    // GROUP SUMMARY
    // =================================================

    private static void showGroupSummary(
            Context context,
            NotificationManager manager
    ) {

        NotificationCompat.Builder summary =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.stat_notify_error)
                        .setContentTitle("Rakshak SMS Alerts")
                        .setContentText("Multiple suspicious SMS detected")
                        .setStyle(new NotificationCompat.InboxStyle())
                        .setGroup(GROUP_SMS)
                        .setGroupSummary(true)
                        .setAutoCancel(true);

        manager.notify(SUMMARY_ID, summary.build());
    }

    // =================================================
    // CHANNEL CREATION
    // =================================================

    private static void createChannelIfNeeded(
            NotificationManager manager,
            RiskResult result
    ) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel existing =
                    manager.getNotificationChannel(CHANNEL_ID);

            if (existing != null) return;

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            CHANNEL_NAME,
                            NotificationManager.IMPORTANCE_HIGH
                    );

            channel.setDescription("Security warnings generated by Rakshak");
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLightColor(getRiskColor(result.getRiskLevel()));

            manager.createNotificationChannel(channel);
        }
    }

    // =================================================
    // PERMISSION CHECK (Android 13+)
    // =================================================

    private static boolean hasNotificationPermission(Context context) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }

        return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED;
    }

    // =================================================
    // HELPERS
    // =================================================

    private static int generateNotificationId() {
        return (int) (System.currentTimeMillis() & 0xFFFFFF);
    }

    private static int getRiskColor(RiskResult.RiskLevel level) {

        if (level == null) return Color.GREEN;

        switch (level) {
            case HIGH:
                return Color.RED;

            case MEDIUM:
                return Color.rgb(255, 165, 0); // Orange

            case LOW:
            default:
                return Color.GREEN;
        }
    }

    private static int getRiskIcon(RiskResult.RiskLevel level) {

        if (level == null) {
            return android.R.drawable.stat_notify_error;
        }

        switch (level) {
            case HIGH:
                return android.R.drawable.stat_notify_error;

            case MEDIUM:
                return android.R.drawable.stat_sys_warning;

            case LOW:
            default:
                return android.R.drawable.stat_notify_more;
        }
    }
}
