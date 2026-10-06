package com.edii.eapregistration;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;

import java.io.ByteArrayOutputStream;

public class SignaturePadView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private boolean hasSignature = false;
    private float lastX;
    private float lastY;

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
        setFocusable(true);
        setFocusableInTouchMode(true);
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

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                path.moveTo(x, y);
                lastX = x;
                lastY = y;
                hasSignature = true;
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                getParent().requestDisallowInterceptTouchEvent(true);
                float midX = (lastX + x) / 2f;
                float midY = (lastY + y) / 2f;
                path.quadTo(lastX, lastY, midX, midY);
                lastX = x;
                lastY = y;
                hasSignature = true;
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
                path.lineTo(x, y);
                getParent().requestDisallowInterceptTouchEvent(false);
                invalidate();
                return true;

            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                return true;

            default:
                return super.onTouchEvent(event);
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

        Bitmap full = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(full);
        canvas.drawColor(Color.WHITE);
        draw(canvas);

        RectF bounds = new RectF();
        path.computeBounds(bounds, true);

        int pad = Math.max(12, Math.round(paint.getStrokeWidth() * 3f));
        int left = Math.max(0, (int) Math.floor(bounds.left) - pad);
        int top = Math.max(0, (int) Math.floor(bounds.top) - pad);
        int right = Math.min(full.getWidth(), (int) Math.ceil(bounds.right) + pad);
        int bottom = Math.min(full.getHeight(), (int) Math.ceil(bounds.bottom) + pad);

        if (right <= left || bottom <= top) return "";

        Bitmap cropped = Bitmap.createBitmap(full, left, top, right - left, bottom - top);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        cropped.compress(Bitmap.CompressFormat.PNG, 100, out);

        if (cropped != full) cropped.recycle();
        full.recycle();

        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
    }
}
