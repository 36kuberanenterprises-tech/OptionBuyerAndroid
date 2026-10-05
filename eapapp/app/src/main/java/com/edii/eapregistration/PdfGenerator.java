package com.edii.eapregistration;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PdfGenerator {

    public static class Result {
        public final String fileName;
        public final String base64;
        public final String savedLocation;

        Result(String fileName, String base64, String savedLocation) {
            this.fileName = fileName;
            this.base64 = base64;
            this.savedLocation = savedLocation;
        }
    }

    public static Result createAndSave(Context context, JSONObject data, String requestId) throws Exception {
        byte[] bytes = createPdfBytes(data, requestId);

        String cleanName = sanitize(data.optString("name", "Participant"));
        String mobile = sanitize(data.optString("mobile", ""));
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String fileName = "EAP_" + cleanName + (mobile.isEmpty() ? "" : "_" + mobile) + "_" + stamp + ".pdf";

        String location = saveToDevice(context, bytes, fileName);
        String b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
        return new Result(fileName, b64, location);
    }

    private static byte[] createPdfBytes(JSONObject data, String requestId) throws Exception {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint title = new Paint(Paint.ANTI_ALIAS_FLAG);
        title.setTextSize(20f);
        title.setFakeBoldText(true);

        Paint section = new Paint(Paint.ANTI_ALIAS_FLAG);
        section.setTextSize(13f);
        section.setFakeBoldText(true);

        Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);
        label.setTextSize(10.5f);
        label.setFakeBoldText(true);

        Paint value = new Paint(Paint.ANTI_ALIAS_FLAG);
        value.setTextSize(10.5f);

        Paint rule = new Paint(Paint.ANTI_ALIAS_FLAG);
        rule.setStrokeWidth(1f);

        canvas.drawText("EAP Registration Form", 36, 48, title);
        canvas.drawText("Registration Reference: " + requestId, 36, 69, value);
        canvas.drawLine(36, 78, 559, 78, rule);

        drawPhoto(canvas, data.optString("photoBase64", ""));

        int y = 105;
        y = field(canvas, label, value, "EAP Date", data.optString("eapDate"), 36, y, 360);
        y = field(canvas, label, value, "Name", data.optString("name"), 36, y, 360);
        y = field(canvas, label, value, "Gender", data.optString("gender"), 36, y, 360);
        y = field(canvas, label, value, "Date of Birth", data.optString("dob"), 36, y, 360);
        y = field(canvas, label, value, "Age on EAP Date", data.optString("ageOnEapDate"), 36, y, 360);

        y += 8;
        canvas.drawText("Address", 36, y, section);
        y += 20;
        y = field(canvas, label, value, "Village", data.optString("village"), 36, y, 500);
        y = field(canvas, label, value, "Panchayat", data.optString("panchayat"), 36, y, 500);
        y = field(canvas, label, value, "Block", data.optString("block"), 36, y, 500);
        y = field(canvas, label, value, "City", data.optString("city"), 36, y, 500);
        y = field(canvas, label, value, "State", data.optString("state"), 36, y, 500);

        y += 8;
        canvas.drawText("Contact and Profile", 36, y, section);
        y += 20;
        y = field(canvas, label, value, "Mobile Number", data.optString("mobile"), 36, y, 500);
        y = field(canvas, label, value, "Alternate Mobile Number", data.optString("alternateMobile"), 36, y, 500);
        y = field(canvas, label, value, "Email ID", data.optString("email"), 36, y, 500);
        y = field(canvas, label, value, "Government ID Type", data.optString("idType"), 36, y, 500);
        y = field(canvas, label, value, "Government ID Number", data.optString("idNumber"), 36, y, 500);
        y = field(canvas, label, value, "Highest Educational Qualification", data.optString("education"), 36, y, 500);
        y = field(canvas, label, value, "Occupation", data.optString("occupation"), 36, y, 500);
        y = field(canvas, label, value, "Individual Income", data.optString("individualIncome"), 36, y, 500);
        y = field(canvas, label, value, "Category", data.optString("category"), 36, y, 500);
        y = field(canvas, label, value, "Intention for EAP", data.optString("intention"), 36, y, 500);
        y = field(canvas, label, value, "Preferred Sector", data.optString("sectors"), 36, y, 500);
        y = field(canvas, label, value, "MSDP Interest", data.optString("msdpInterest"), 36, y, 500);

        y += 10;
        canvas.drawLine(36, y, 559, y, rule);
        y += 20;
        canvas.drawText("Declaration", 36, y, section);
        y += 18;
        y = wrapped(canvas, value,
                "I declare that the information provided by me is correct. I will be responsible for any discrepancy detected.",
                36, y, 520, 14);

        y += 12;
        canvas.drawText("Submitted through EAP Registration Android Application", 36, y, value);

        document.finishPage(page);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.writeTo(out);
        document.close();
        return out.toByteArray();
    }

    private static void drawPhoto(Canvas canvas, String base64) {
        if (base64 == null || base64.trim().isEmpty()) return;
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bitmap != null) {
                RectF box = new RectF(420, 92, 550, 210);
                canvas.drawBitmap(bitmap, null, box, new Paint(Paint.ANTI_ALIAS_FLAG));
                Paint border = new Paint(Paint.ANTI_ALIAS_FLAG);
                border.setStyle(Paint.Style.STROKE);
                border.setStrokeWidth(1f);
                canvas.drawRect(box, border);
            }
        } catch (Exception ignored) {}
    }

    private static int field(Canvas canvas, Paint label, Paint value, String key, String val,
                             int x, int y, int maxWidth) {
        String safe = val == null || val.trim().isEmpty() ? "Not provided" : val.trim();
        canvas.drawText(key + ":", x, y, label);
        float labelWidth = label.measureText(key + ":") + 8;
        int valueX = x + (int) labelWidth;
        int available = Math.max(120, maxWidth - (int) labelWidth);
        int nextY = wrapped(canvas, value, safe, valueX, y, available, 14);
        return Math.max(y + 20, nextY + 6);
    }

    private static int wrapped(Canvas canvas, Paint paint, String text, int x, int y,
                               int maxWidth, int lineHeight) {
        if (text == null) text = "";
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        int currentY = y;
        for (String word : words) {
            String test = line.length() == 0 ? word : line + " " + word;
            if (paint.measureText(test) > maxWidth && line.length() > 0) {
                canvas.drawText(line.toString(), x, currentY, paint);
                currentY += lineHeight;
                line = new StringBuilder(word);
            } else {
                if (line.length() > 0) line.append(" ");
                line.append(word);
            }
        }
        if (line.length() > 0) {
            canvas.drawText(line.toString(), x, currentY, paint);
        }
        return currentY;
    }

    private static String saveToDevice(Context context, byte[] bytes, String fileName) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/EAP Registrations");
            values.put(MediaStore.MediaColumns.IS_PENDING, 1);

            ContentResolver resolver = context.getContentResolver();
            Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) throw new Exception("Unable to create PDF in Downloads.");

            try (OutputStream os = resolver.openOutputStream(uri)) {
                if (os == null) throw new Exception("Unable to open PDF output.");
                os.write(bytes);
            }

            values.clear();
            values.put(MediaStore.MediaColumns.IS_PENDING, 0);
            resolver.update(uri, values, null, null);
            return "Downloads/EAP Registrations/" + fileName;
        }

        File base = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (base == null) base = context.getFilesDir();
        File folder = new File(base, "EAP Registrations");
        if (!folder.exists() && !folder.mkdirs()) {
            throw new Exception("Unable to create PDF folder.");
        }
        File file = new File(folder, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(bytes);
        }
        return file.getAbsolutePath();
    }

    private static String sanitize(String value) {
        if (value == null) return "";
        String cleaned = value.trim().replaceAll("[^A-Za-z0-9_ ]", "");
        cleaned = cleaned.replaceAll("\\s+", "_");
        if (cleaned.length() > 35) cleaned = cleaned.substring(0, 35);
        return cleaned;
    }
}
