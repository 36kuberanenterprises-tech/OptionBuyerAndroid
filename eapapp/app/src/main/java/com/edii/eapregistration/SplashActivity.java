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
    private static final int BORDER = Color.rgb(224, 231, 225);
    private static final int SOFT_GREEN = Color.rgb(244, 251, 244);

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
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(22), dp(28), dp(22), dp(28));
        root.setBackgroundColor(Color.WHITE);

        root.setOnApplyWindowInsetsListener((v, insets) -> {
            int top = insets.getSystemWindowInsetTop();
            int bottom = insets.getSystemWindowInsetBottom();
            v.setPadding(dp(22), dp(28) + top, dp(22), dp(28) + bottom);
            return insets.consumeSystemWindowInsets();
        });
        root.requestApplyInsets();

        LinearLayout spacerTop = new LinearLayout(this);
        root.addView(spacerTop, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 0.55f));

        ImageView ediiLogo = new ImageView(this);
        ediiLogo.setImageResource(R.drawable.ic_edii_launcher);
        ediiLogo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        ediiLogo.setAlpha(0f);
        ediiLogo.setScaleX(0.72f);
        ediiLogo.setScaleY(0.72f);
        ediiLogo.setTranslationY(dp(18));
        root.addView(ediiLogo, new LinearLayout.LayoutParams(dp(174), dp(174)));

        TextView appTitle = new TextView(this);
        appTitle.setText("HAL EAP Registration");
        appTitle.setTextColor(TEXT);
        appTitle.setTextSize(24);
        appTitle.setTypeface(Typeface.DEFAULT_BOLD);
        appTitle.setGravity(Gravity.CENTER);
        appTitle.setPadding(0, dp(8), 0, 0);
        appTitle.setAlpha(0f);
        appTitle.setTranslationY(dp(16));
        root.addView(appTitle);

        View accent = new View(this);
        accent.setBackgroundColor(GREEN);
        accent.setScaleX(0f);
        LinearLayout.LayoutParams accentLp = new LinearLayout.LayoutParams(dp(84), dp(3));
        accentLp.setMargins(0, dp(10), 0, dp(10));
        root.addView(accent, accentLp);

        TextView subtitle = new TextView(this);
        subtitle.setText("Entrepreneurship Awareness Programme");
        subtitle.setTextColor(MUTED);
        subtitle.setTextSize(14);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setAlpha(0f);
        subtitle.setTranslationY(dp(10));
        root.addView(subtitle);

        LinearLayout partnerCard = new LinearLayout(this);
        partnerCard.setOrientation(LinearLayout.HORIZONTAL);
        partnerCard.setGravity(Gravity.CENTER);
        partnerCard.setPadding(dp(12), dp(12), dp(12), dp(12));
        partnerCard.setBackground(roundRect(SOFT_GREEN, dp(18), BORDER, 1));
        partnerCard.setAlpha(0f);
        partnerCard.setTranslationY(dp(20));

        LinearLayout ediiBlock = new LinearLayout(this);
        ediiBlock.setOrientation(LinearLayout.VERTICAL);
        ediiBlock.setGravity(Gravity.CENTER_HORIZONTAL);
        ediiBlock.setPadding(dp(4), dp(2), dp(6), dp(2));

        ImageView smallEdii = new ImageView(this);
        smallEdii.setImageResource(R.drawable.ic_edii_app);
        smallEdii.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        ediiBlock.addView(smallEdii, new LinearLayout.LayoutParams(dp(68), dp(68)));

        TextView organised = new TextView(this);
        organised.setText("Organised by");
        organised.setTextColor(MUTED);
        organised.setTextSize(11);
        organised.setGravity(Gravity.CENTER);
        ediiBlock.addView(organised);

        TextView ediiName = new TextView(this);
        ediiName.setText("EDII");
        ediiName.setTextColor(GREEN_DARK);
        ediiName.setTextSize(15);
        ediiName.setTypeface(Typeface.DEFAULT_BOLD);
        ediiName.setGravity(Gravity.CENTER);
        ediiBlock.addView(ediiName);

        View divider = new View(this);
        divider.setBackgroundColor(BORDER);

        LinearLayout halBlock = new LinearLayout(this);
        halBlock.setOrientation(LinearLayout.VERTICAL);
        halBlock.setGravity(Gravity.CENTER_HORIZONTAL);
        halBlock.setPadding(dp(10), dp(2), dp(4), dp(2));

        ImageView halLogo = new ImageView(this);
        halLogo.setImageResource(R.drawable.hal_sponsor_logo);
        halLogo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        halBlock.addView(halLogo, new LinearLayout.LayoutParams(dp(128), dp(72)));

        TextView sponsored = new TextView(this);
        sponsored.setText("Sponsored by HAL");
        sponsored.setTextColor(TEXT);
        sponsored.setTextSize(12);
        sponsored.setTypeface(Typeface.DEFAULT_BOLD);
        sponsored.setGravity(Gravity.CENTER);
        sponsored.setPadding(0, dp(3), 0, 0);
        halBlock.addView(sponsored);

        partnerCard.addView(ediiBlock, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        LinearLayout.LayoutParams dividerLp = new LinearLayout.LayoutParams(
                dp(1), dp(102));
        dividerLp.setMargins(dp(3), 0, dp(3), 0);
        partnerCard.addView(divider, dividerLp);

        partnerCard.addView(halBlock, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.35f));

        LinearLayout.LayoutParams partnerLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        partnerLp.setMargins(dp(4), dp(24), dp(4), 0);
        root.addView(partnerCard, partnerLp);

        TextView footer = new TextView(this);
        footer.setText("Micro Skillpreneurship Development Programme");
        footer.setTextColor(MUTED);
        footer.setTextSize(11);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(18), 0, 0);
        footer.setAlpha(0f);
        root.addView(footer);

        LinearLayout spacerBottom = new LinearLayout(this);
        root.addView(spacerBottom, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 0.75f));

        setContentView(root);

        AnimatorSet logoAnim = new AnimatorSet();
        logoAnim.playTogether(
                ObjectAnimator.ofFloat(ediiLogo, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(ediiLogo, View.SCALE_X, 0.72f, 1.06f, 1f),
                ObjectAnimator.ofFloat(ediiLogo, View.SCALE_Y, 0.72f, 1.06f, 1f),
                ObjectAnimator.ofFloat(ediiLogo, View.TRANSLATION_Y, dp(18), 0f)
        );
        logoAnim.setDuration(620);
        logoAnim.setInterpolator(new DecelerateInterpolator());

        AnimatorSet titleAnim = new AnimatorSet();
        titleAnim.playTogether(
                ObjectAnimator.ofFloat(appTitle, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(appTitle, View.TRANSLATION_Y, dp(16), 0f),
                ObjectAnimator.ofFloat(accent, View.SCALE_X, 0f, 1f)
        );
        titleAnim.setDuration(420);
        titleAnim.setStartDelay(420);
        titleAnim.setInterpolator(new DecelerateInterpolator());

        AnimatorSet subtitleAnim = new AnimatorSet();
        subtitleAnim.playTogether(
                ObjectAnimator.ofFloat(subtitle, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(subtitle, View.TRANSLATION_Y, dp(10), 0f)
        );
        subtitleAnim.setDuration(340);
        subtitleAnim.setStartDelay(720);

        AnimatorSet partnerAnim = new AnimatorSet();
        partnerAnim.playTogether(
                ObjectAnimator.ofFloat(partnerCard, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(partnerCard, View.TRANSLATION_Y, dp(20), 0f),
                ObjectAnimator.ofFloat(footer, View.ALPHA, 0f, 1f)
        );
        partnerAnim.setDuration(460);
        partnerAnim.setStartDelay(960);
        partnerAnim.setInterpolator(new DecelerateInterpolator());

        logoAnim.start();
        titleAnim.start();
        subtitleAnim.start();
        partnerAnim.start();

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
        }, 2450);
    }

    private GradientDrawable roundRect(int fill, float radius, int strokeColor, int strokeWidth) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(radius);
        if (strokeWidth > 0) {
            drawable.setStroke(dp(strokeWidth), strokeColor);
        }
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
