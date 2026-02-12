package com.rakshak.security.filescanner;

import android.content.Context;
import android.net.Uri;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class FileFeatureUtil {

    // ==========================================
    // Calculate Shannon Entropy of file
    // ==========================================
    public static double calculateEntropyFromUri(
            Context context,
            Uri uri
    ) {

        try {

            InputStream inputStream =
                    context.getContentResolver()
                            .openInputStream(uri);

            if (inputStream == null) return 0;

            byte[] buffer = new byte[4096];
            int bytesRead;

            Map<Byte, Integer> byteCounts =
                    new HashMap<>();

            int totalBytes = 0;

            while ((bytesRead = inputStream.read(buffer)) != -1) {

                for (int i = 0; i < bytesRead; i++) {
                    byte b = buffer[i];

                    byteCounts.put(
                            b,
                            byteCounts.getOrDefault(b, 0) + 1
                    );
                }

                totalBytes += bytesRead;
            }

            inputStream.close();

            if (totalBytes == 0) return 0;

            double entropy = 0.0;

            for (int count : byteCounts.values()) {

                double probability =
                        (double) count / totalBytes;

                entropy -= probability *
                        (Math.log(probability) / Math.log(2));
            }

            return entropy;

        } catch (Exception e) {
            return 0;
        }
    }
}
