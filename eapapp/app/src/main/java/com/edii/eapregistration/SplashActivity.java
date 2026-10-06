package com.edii.eapregistration;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR |
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(28), dp(36), dp(28), dp(36));
        root.setBackgroundColor(Color.WHITE);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.edii_app_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        logo.setAlpha(0f);
        logo.setScaleX(0.84f);
        logo.setScaleY(0.84f);
        root.addView(logo, new LinearLayout.LayoutParams(dp(190), dp(190)));

        TextView title = new TextView(this);
        title.setText("HAL EAP Registration");
        title.setTextColor(Color.rgb(30, 37, 32));
        title.setTextSize(23);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(16), 0, 0);
        title.setAlpha(0f);
        title.setTranslationY(dp(10));
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Entrepreneurship Awareness Programme");
        subtitle.setTextColor(Color.rgb(103, 112, 106));
        subtitle.setTextSize(14);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, dp(7), 0, 0);
        subtitle.setAlpha(0f);
        subtitle.setTranslationY(dp(10));
        root.addView(subtitle);

        TextView loading = new TextView(this);
        loading.setText("EDII");
        loading.setTextColor(Color.rgb(40, 153, 50));
        loading.setTextSize(13);
        loading.setTypeface(Typeface.DEFAULT_BOLD);
        loading.setGravity(Gravity.CENTER);
        loading.setPadding(0, dp(22), 0, 0);
        loading.setAlpha(0f);
        root.addView(loading);

        setContentView(root);

        AnimatorSet logoSet = new AnimatorSet();
        logoSet.playTogether(
                ObjectAnimator.ofFloat(logo, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(logo, View.SCALE_X, 0.84f, 1f),
                ObjectAnimator.ofFloat(logo, View.SCALE_Y, 0.84f, 1f)
        );
        logoSet.setDuration(520);
        logoSet.setInterpolator(new DecelerateInterpolator());

        AnimatorSet titleSet = new AnimatorSet();
        titleSet.playTogether(
                ObjectAnimator.ofFloat(title, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(title, View.TRANSLATION_Y, dp(10), 0f)
        );
        titleSet.setDuration(360);
        titleSet.setStartDelay(260);

        AnimatorSet subtitleSet = new AnimatorSet();
        subtitleSet.playTogether(
                ObjectAnimator.ofFloat(subtitle, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(subtitle, View.TRANSLATION_Y, dp(10), 0f),
                ObjectAnimator.ofFloat(loading, View.ALPHA, 0f, 1f)
        );
        subtitleSet.setDuration(360);
        subtitleSet.setStartDelay(430);

        logoSet.start();
        titleSet.start();
        subtitleSet.start();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 1450);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
