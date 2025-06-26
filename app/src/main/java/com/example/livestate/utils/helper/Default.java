package com.example.livestate.utils.helper;

import android.Manifest;
import android.os.Build;

public class Default {
    //about app
    public static final String EMAIL = "tranduyhung171199@gmail.com";
    public static final String SUBJECT = "Feedback: All Soccer Lives Cores";
    public static final String PRIVACY_POLICY = "https://sites.google.com/view/allsoccerlivescores/home";

        public static final String[] STORAGE_PERMISSION = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            ? new String[]{Manifest.permission.READ_MEDIA_VIDEO,Manifest.permission.READ_MEDIA_AUDIO}
            : new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
    public static final String RECORD_PERMISSION = Manifest.permission.RECORD_AUDIO;
}
