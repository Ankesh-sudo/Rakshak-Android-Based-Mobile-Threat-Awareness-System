package com.rakshak.security.filescanner;

import android.app.Activity;
import android.content.Intent;

public class FilePickerHelper {

    public static final int PICK_FILE_REQUEST = 101;

    public static void pickFile(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        activity.startActivityForResult(intent, PICK_FILE_REQUEST);
    }
}
