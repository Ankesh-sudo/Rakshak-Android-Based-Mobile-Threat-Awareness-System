package com.rakshak.security.calls.engine.detectors;

import android.content.Context;

import com.rakshak.security.calls.data.CallHistoryManager;

public class FrequencyDetector {

    public static int analyze(Context context, String number) {

        int count = CallHistoryManager.getCallCount(context, number);

        if (count > 10)
            return 40;

        if (count > 5)
            return 20;

        return 0;
    }
}
