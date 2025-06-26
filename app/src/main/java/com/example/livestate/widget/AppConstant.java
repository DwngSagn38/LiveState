package com.example.livestate.widget;

import android.content.Context;
import android.os.Environment;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kotlin.jvm.internal.Intrinsics;

public final class AppConstant {
    public static final String actionService = "action_service";
    public static final String actionStart = "action_start";
    public static final String actionStop = "action_stop";
    public static final String channelId = "save_audio_service";
    public static final String channelName = "save audio service";
    public static final AppConstant APP_CONSTANT = new AppConstant();
    private static final String keyPathVoice = "key_path_voice";
    private static final String keyPositionEffect = "key_position_effect";
    private static final String keyScreenIntoVoiceEffect = "key_screen_into_voice_effect";
    private static final String keySizeVoice = "key_size_effect";
    private static final String keyDurationVoice = "key_duration_effect";
    private static final String FILE_MUSIC = Environment.getExternalStorageDirectory().getPath() + "/" + Environment.DIRECTORY_MUSIC;
    private AppConstant() {
    }

    public String getKEY_DURATION_VOICE() {
        return keyDurationVoice;
    }

    public String getKEY_SIZE_VOICE() {
        return keySizeVoice;
    }

    public String getKEY_PATH_VOICE() {
        return keyPathVoice;
    }

    public String getKEY_POSITION_EFFECT() {
        return keyPositionEffect;
    }

    public String getKEY_FILENAME_EFFECT() {
        String KEY_FILENAME_EFFECT = "key_filename_effect";
        return KEY_FILENAME_EFFECT;
    }
    public static String getKEY_FILE_MUSIC() {
        return FILE_MUSIC;
    }

    public String getKEY_SCREEN_INTO_VOICE_EFFECTS() {
        return keyScreenIntoVoiceEffect;
    }

    public void setCheckResumePermissionMain(boolean z) {
    }

    public void setCheckResumePermissionRingtone(boolean z) {
    }

    public void setCheckResumeShareMyVoice(boolean z) {
    }

    public void setCheckResumeShare(boolean z) {
    }

    public String getVoiceEffect(Context mCtx) {
        try {
            InputStream inputStream = mCtx.getAssets().open("effects.json");
            Intrinsics.checkNotNullExpressionValue(inputStream, "context.assets.open(\"effects.json\")");
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            inputStream.close();
            Charset utf8 = StandardCharsets.UTF_8;
            Intrinsics.checkNotNullExpressionValue(utf8, "UTF_8");
            return new String(bytes, utf8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static boolean getSpecialChar(String strSpecial) {
        Pattern pattern = Pattern.compile("[^A-Za-z0-9_ ]");
        Intrinsics.checkNotNullExpressionValue(pattern, "compile(\"[^A-Za-z0-9_ ]\")");
        Matcher matchers = pattern.matcher(strSpecial);
        Intrinsics.checkNotNullExpressionValue(matchers, "p.matcher(s)");
        return matchers.find();
    }
}
