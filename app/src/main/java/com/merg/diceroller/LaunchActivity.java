package com.merg.diceroller;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

public final class LaunchActivity extends AppCompatActivity {
    private static final long INTRO_MS = 850L;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable openMain = () -> {
        startActivity(new Intent(this, MainActivity.class));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        TextView versionText = findViewById(R.id.versionText);
        versionText.setText(getString(R.string.version_format, BuildConfig.VERSION_NAME));
        animateIntro();
    }

    @Override protected void onDestroy() {
        handler.removeCallbacks(openMain);
        super.onDestroy();
    }

    private void animateIntro() {
        View logo = findViewById(R.id.brandLogo);
        View appName = findViewById(R.id.appNameText);
        View footer = findViewById(R.id.footerGroup);
        logo.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(240L).start();
        appName.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(240L).start();
        footer.animate()
                .alpha(1f)
                .setStartDelay(90L)
                .setDuration(220L)
                .withEndAction(() -> handler.postDelayed(openMain, INTRO_MS))
                .start();
    }
}
