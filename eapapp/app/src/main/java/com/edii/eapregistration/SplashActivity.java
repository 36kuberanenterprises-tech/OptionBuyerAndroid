package com.edii.eapregistration;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(28), dp(28), dp(28), dp(28));
        root.setBackgroundColor(Color.WHITE);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.ic_edii_app);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        root.addView(logo, new LinearLayout.LayoutParams(dp(150), dp(150)));

        TextView edii = new TextView(this);
        edii.setText("EDII");
        edii.setTextColor(Color.rgb(30, 37, 32));
        edii.setTextSize(30);
        edii.setTypeface(Typeface.DEFAULT_BOLD);
        edii.setGravity(Gravity.CENTER);
        edii.setPadding(0, dp(8), 0, 0);
        root.addView(edii);

        TextView title = new TextView(this);
        title.setText("HAL EAP Registration");
        title.setTextColor(Color.rgb(30, 37, 32));
        title.setTextSize(22);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(10), 0, 0);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Entrepreneurship Awareness Programme");
        subtitle.setTextColor(Color.rgb(103, 112, 106));
        subtitle.setTextSize(14);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, dp(8), 0, 0);
        root.addView(subtitle);

        setContentView(root);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }, 900);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
