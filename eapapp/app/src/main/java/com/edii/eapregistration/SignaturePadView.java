package com.edii.eapregistration;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.ByteArrayOutputStream;
import android.util.Base64;

public class SignaturePadView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private boolean hasSignature = false;

    public SignaturePadView(Context context) {
        super(context);
        init();
    }

    public SignaturePadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        setBackgroundColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);
                hasSignature = true;
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                hasSignature = true;
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
                path.lineTo(x, y);
                invalidate();
                return true;

            default:
                return false;
        }
    }

    public void clear() {
        path.reset();
        hasSignature = false;
        invalidate();
    }

    public boolean hasSignature() {
        return hasSignature;
    }

    public String toBase64() {
        if (!hasSignature || getWidth() <= 0 || getHeight() <= 0) return "";

        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        draw(canvas);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
    }
}
