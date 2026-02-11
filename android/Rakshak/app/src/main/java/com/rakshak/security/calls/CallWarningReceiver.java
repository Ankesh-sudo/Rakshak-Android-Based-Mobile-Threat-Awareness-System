package com.rakshak.security.calls;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.rakshak.security.calls.ui.CallWarningActivity;

public class CallWarningReceiver extends BroadcastReceiver {

    private static final String TAG = "RAKSHAK_WARNING";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent == null) return;

        String number = intent.getStringExtra("number");
        int score = intent.getIntExtra("score", 0);

        Log.d(TAG, "Launching warning activity");

        Intent activityIntent =
                new Intent(context, CallWarningActivity.class);

        activityIntent.putExtra("number", number);
        activityIntent.putExtra("score", score);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(activityIntent);
    }
}
