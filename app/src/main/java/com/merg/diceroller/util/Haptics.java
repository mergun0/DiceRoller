package com.merg.diceroller.util;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.View;

public final class Haptics {
    private Haptics() {}
    public static void tick(Context context, View fallback) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= 26) vibrator.vibrate(VibrationEffect.createOneShot(28, VibrationEffect.DEFAULT_AMPLITUDE));
            else vibrator.vibrate(28);
        } else if (fallback != null) {
            fallback.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        }
    }
}
