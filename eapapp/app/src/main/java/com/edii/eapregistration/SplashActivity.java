package com.edii.eapregistration;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
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

    private static final int GREEN = Color.rgb(40, 153, 50);
    private static final int GREEN_DARK = Color.rgb(28, 125, 39);
    private static final int TEXT = Color.rgb(30, 37, 32);
    private static final int MUTED = Color.rgb(103, 112, 106);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR |
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(28), dp(28), dp(28), dp(28));
        root.setBackgroundColor(Color.WHITE);

        root.setOnApplyWindowInsetsListener((v, insets) -> {
            int top = insets.getSystemWindowInsetTop();
            int bottom = insets.getSystemWindowInsetBottom();
            v.setPadding(dp(28), dp(28) + top, dp(28), dp(28) + bottom);
            return insets.consumeSystemWindowInsets();
        });
        root.requestApplyInsets();

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.ic_edii_launcher);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        logo.setAlpha(0f);
        logo.setScaleX(0.65f);
        logo.setScaleY(0.65f);
        logo.setTranslationY(dp(18));
        root.addView(logo, new LinearLayout.LayoutParams(dp(220), dp(220)));

        TextView title = new TextView(this);
        title.setText("HAL EAP Registration");
        title.setTextColor(TEXT);
        title.setTextSize(24);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(18), 0, 0);
        title.setAlpha(0f);
        title.setTranslationY(dp(18));
        root.addView(title);

        View accent = new View(this);
        accent.setBackgroundColor(GREEN);
        accent.setScaleX(0f);
        LinearLayout.LayoutParams accentLp = new LinearLayout.LayoutParams(dp(92), dp(3));
        accentLp.setMargins(0, dp(10), 0, dp(9));
        root.addView(accent, accentLp);

        TextView subtitle = new TextView(this);
        subtitle.setText("Entrepreneurship Awareness Programme");
        subtitle.setTextColor(MUTED);
        subtitle.setTextSize(14);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setAlpha(0f);
        subtitle.setTranslationY(dp(12));
        root.addView(subtitle);

        TextView powered = new TextView(this);
        powered.setText("Entrepreneurship Development Institute of India");
        powered.setTextColor(GREEN_DARK);
        powered.setTextSize(12);
        powered.setTypeface(Typeface.DEFAULT_BOLD);
        powered.setGravity(Gravity.CENTER);
        powered.setPadding(0, dp(22), 0, 0);
        powered.setAlpha(0f);
        root.addView(powered);

        setContentView(root);

        AnimatorSet logoSet = new AnimatorSet();
        logoSet.playTogether(
                ObjectAnimator.ofFloat(logo, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(logo, View.SCALE_X, 0.65f, 1.08f, 1f),
                ObjectAnimator.ofFloat(logo, View.SCALE_Y, 0.65f, 1.08f, 1f),
                ObjectAnimator.ofFloat(logo, View.TRANSLATION_Y, dp(18), 0f)
        );
        logoSet.setDuration(720);
        logoSet.setInterpolator(new DecelerateInterpolator());

        AnimatorSet titleSet = new AnimatorSet();
        titleSet.playTogether(
                ObjectAnimator.ofFloat(title, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(title, View.TRANSLATION_Y, dp(18), 0f),
                ObjectAnimator.ofFloat(accent, View.SCALE_X, 0f, 1f)
        );
        titleSet.setDuration(450);
        titleSet.setStartDelay(520);
        titleSet.setInterpolator(new DecelerateInterpolator());

        AnimatorSet textSet = new AnimatorSet();
        textSet.playTogether(
                ObjectAnimator.ofFloat(subtitle, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(subtitle, View.TRANSLATION_Y, dp(12), 0f),
                ObjectAnimator.ofFloat(powered, View.ALPHA, 0f, 1f)
        );
        textSet.setDuration(420);
        textSet.setStartDelay(900);
        textSet.setInterpolator(new DecelerateInterpolator());

        logoSet.start();
        titleSet.start();
        textSet.start();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            root.animate()
                    .alpha(0f)
                    .setDuration(220)
                    .withEndAction(() -> {
                        startActivity(new Intent(this, MainActivity.class));
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    })
                    .start();
        }, 1850);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
