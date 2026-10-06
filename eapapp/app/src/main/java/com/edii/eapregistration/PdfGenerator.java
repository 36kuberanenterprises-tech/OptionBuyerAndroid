package com.edii.eapregistration;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
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

    private static final int PAGE_W = 595;
    private static final int PAGE_H = 842;

    private static final String HAL_LOGO_B64 =
            "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";

    private static final String EDII_LOGO_B64 =
            "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";

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
        byte[] bytes = createPdfBytes(data);

        String cleanName = sanitize(data.optString("name", "Participant"));
        String mobile = sanitize(data.optString("mobile", ""));
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String fileName = "HAL_EAP_Registration_" + cleanName +
                (mobile.isEmpty() ? "" : "_" + mobile) + "_" + stamp + ".pdf";

        String location = saveToDevice(context, bytes, fileName);
        String b64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
        return new Result(fileName, b64, location);
    }

    private static byte[] createPdfBytes(JSONObject data) throws Exception {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas c = page.getCanvas();

        Paint regular = paint(12f, false);
        Paint bold = paint(12f, true);
        Paint title = paint(14f, true);
        Paint small = paint(11f, false);
        Paint smallBold = paint(11f, true);
        Paint value = paint(10.5f, false);
        Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(1f);

        drawLogo(c, HAL_LOGO_B64, new RectF(28, 5, 105, 56));
        drawLogo(c, EDII_LOGO_B64, new RectF(472, 7, 570, 53));

        drawCentered(c, "Entrepreneurship Awareness Programme (EAP)", title, 300, 21);
        drawCentered(c, "Organized by", regular, 300, 43);
        drawCentered(c, "Hindustan Aeronautics Limited (HAL)", bold, 300, 73);
        drawCentered(c, "In Collaboration with", regular, 300, 88);
        drawCentered(c, "Entrepreneurship Development Institute of India (EDII)", bold, 300, 103);
        drawCentered(c, "Website: www.ediindia.org, www. ediindia.ac.in", small, 300, 117);

        drawCentered(c, repeat('*', 88), small, 300, 138);
        drawCentered(c, "Registration Form", bold, 300, 154);

        c.drawText("Date:", 83, 177, regular);
        drawFit(c, safe(data.optString("eapDate")), 118, 177, 150, value, 8f);
        c.drawText("Place:", 410, 177, regular);
        drawFit(c, safe(data.optString("place", data.optString("city"))), 452, 177, 112, value, 8f);
        drawCentered(c, repeat('*', 88), small, 300, 198);

        drawPhotoOrPlaceholder(c, data.optString("photoBase64", ""), line, smallBold);

        label(c, regular, "1)", 89, 226);
        label(c, regular, "Full Name:", 143, 226);
        drawFit(c, safe(data.optString("name")), 218, 226, 265, value, 7.5f);

        label(c, regular, "2)", 89, 252);
        label(c, regular, "Gender:", 143, 252);
        drawFit(c, safe(data.optString("gender")), 196, 252, 270, value, 8f);

        label(c, regular, "3)", 89, 274);
        label(c, regular, "Date of Birth:", 143, 274);
        drawFit(c, safe(data.optString("dob")), 232, 274, 230, value, 8f);

        label(c, regular, "4)", 89, 296);
        label(c, regular, "Father's/Husband's/Mother's Name:", 143, 296);
        drawFit(c, safe(data.optString("guardianName")), 368, 296, 116, value, 7f);

        label(c, regular, "5)", 89, 318);
        label(c, regular, "Full Address:", 143, 318);
        drawFit(c, safe(fullAddress(data)), 230, 318, 250, value, 7f);

        label(c, regular, "6)", 89, 340);
        label(c, regular, "Mobile Phone:", 143, 340);
        drawFit(c, safe(data.optString("mobile")), 233, 340, 245, value, 8f);

        label(c, regular, "7)", 89, 362);
        label(c, regular, "Alternative Phone No.:", 143, 362);
        drawFit(c, safe(data.optString("alternateMobile")), 282, 362, 196, value, 8f);

        label(c, regular, "8)", 89, 384);
        label(c, regular, "Email ID:", 143, 384);
        drawFit(c, safe(data.optString("email")), 207, 384, 270, value, 7.5f);

        label(c, regular, "9)", 89, 407);
        label(c, regular, "Aadhar No.:", 143, 407);
        drawFit(c, safe(data.optString("idNumber")), 222, 407, 255, value, 8f);

        label(c, regular, "10)", 89, 429);
        label(c, regular, "Highest Educational Qualification:", 143, 429);
        drawFit(c, safe(data.optString("education")), 353, 429, 125, value, 7f);

        label(c, regular, "11)", 89, 451);
        label(c, regular, "Occupation:", 143, 451);
        drawFit(c, safe(data.optString("occupation")), 220, 451, 255, value, 8f);

        label(c, regular, "12)", 89, 473);
        label(c, regular, "Income (Individual):", 143, 473);
        drawFit(c, safe(data.optString("individualIncome")), 264, 473, 210, value, 8f);

        label(c, regular, "13)", 89, 499);
        label(c, regular, "Category:", 143, 499);
        drawCheckOption(c, 203, 488, "SC", 218, 499, "SC".equalsIgnoreCase(data.optString("category")), regular, line);
        drawCheckOption(c, 238, 488, "ST", 253, 499, "ST".equalsIgnoreCase(data.optString("category")), regular, line);
        drawCheckOption(c, 272, 488, "OBC", 287, 499, "OBC".equalsIgnoreCase(data.optString("category")), regular, line);

        label(c, regular, "14)", 89, 523);
        label(c, regular, "Intention for taking part in the EAP:", 143, 523);
        drawBox(c, 358, 511, 12, line, "Employment".equalsIgnoreCase(data.optString("intention")));
        c.drawText("a) Employment", 373, 523, regular);
        drawBox(c, 363, 526, 12, line, "Self Employment".equalsIgnoreCase(data.optString("intention")) ||
                "Self-employment".equalsIgnoreCase(data.optString("intention")));
        c.drawText("b) Self-employment", 378, 538, regular);

        c.drawText("13)", 86, 560, regular);
        c.drawText("In which sector would you like to start business?", 143, 560, regular);

        String sectors = data.optString("sectors", "");
        drawBox(c, 166, 572, 12, line, containsIgnoreCase(sectors, "Fashion Technology"));
        c.drawText("Fashion technology", 205, 584, regular);
        drawBox(c, 166, 594, 12, line, containsIgnoreCase(sectors, "Food Processing"));
        c.drawText("Food processing", 205, 606, regular);
        drawBox(c, 166, 616, 12, line, containsIgnoreCase(sectors, "Jute Bag Manufacturing"));
        c.drawText("Jute Bag manufacturing", 205, 628, regular);
        drawBox(c, 166, 638, 12, line, containsIgnoreCase(sectors, "Beautician"));
        c.drawText("Beautician", 205, 650, regular);

        c.drawText("14)", 86, 672, regular);
        c.drawText("Do you want to attend Micro Skill Entrepreneurship Development", 143, 672, regular);
        c.drawText("Programme (MSDP) to understand business?", 143, 687, regular);

        boolean msdpYes = "Yes".equalsIgnoreCase(data.optString("msdpInterest"));
        boolean msdpNo = "No".equalsIgnoreCase(data.optString("msdpInterest"));
        drawBox(c, 435, 675, 12, line, msdpYes);
        c.drawText("Yes", 450, 687, regular);
        drawBox(c, 507, 675, 12, line, msdpNo);
        c.drawText("No", 522, 687, regular);

        c.drawText("I declare that the above information provided by me is completely correct. I will be", 58, 716, regular);
        c.drawText("responsible if any discrepancy is detected.", 115, 731, regular);

        RectF sign = new RectF(413, 747, 560, 790);
        c.drawRect(sign, line);
        c.drawText("SIGNATURE", 460, 774, smallBold);

        document.finishPage(page);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.writeTo(out);
        document.close();
        return out.toByteArray();
    }

    private static Paint paint(float size, boolean bold) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTextSize(size);
        p.setTypeface(Typeface.create("serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        return p;
    }

    private static void drawLogo(Canvas c, String b64, RectF rect) {
        try {
            byte[] bytes = Base64.decode(b64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bitmap != null) c.drawBitmap(bitmap, null, rect, new Paint(Paint.ANTI_ALIAS_FLAG));
        } catch (Exception ignored) {}
    }

    private static void drawPhotoOrPlaceholder(Canvas c, String b64, Paint border, Paint label) {
        RectF box = new RectF(493, 214, 568, 315);
        c.drawRect(box, border);
        if (b64 != null && !b64.trim().isEmpty()) {
            try {
                byte[] bytes = Base64.decode(b64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bitmap != null) {
                    RectF inset = new RectF(494, 215, 567, 314);
                    c.drawBitmap(bitmap, null, inset, new Paint(Paint.ANTI_ALIAS_FLAG));
                    return;
                }
            } catch (Exception ignored) {}
        }
        c.drawText("PHOTO", 514, 266, label);
    }

    private static void drawCentered(Canvas c, String text, Paint p, float centerX, float baselineY) {
        c.drawText(text, centerX - p.measureText(text) / 2f, baselineY, p);
    }

    private static void label(Canvas c, Paint p, String text, float x, float y) {
        c.drawText(text, x, y, p);
    }

    private static void drawFit(Canvas c, String text, float x, float y, float maxWidth, Paint base, float minSize) {
        if (text == null || text.trim().isEmpty()) return;
        Paint p = new Paint(base);
        while (p.measureText(text) > maxWidth && p.getTextSize() > minSize) {
            p.setTextSize(p.getTextSize() - 0.25f);
        }
        String fitted = text;
        if (p.measureText(fitted) > maxWidth) {
            while (fitted.length() > 3 && p.measureText(fitted + "...") > maxWidth) {
                fitted = fitted.substring(0, fitted.length() - 1);
            }
            fitted = fitted + "...";
        }
        c.drawText(fitted, x, y, p);
    }

    private static void drawCheckOption(Canvas c, float boxX, float boxY, String text, float textX, float textY,
                                        boolean checked, Paint textPaint, Paint linePaint) {
        drawBox(c, boxX, boxY, 10, linePaint, checked);
        c.drawText(text, textX, textY, textPaint);
    }

    private static void drawBox(Canvas c, float x, float y, float size, Paint linePaint, boolean checked) {
        RectF r = new RectF(x, y, x + size, y + size);
        c.drawRect(r, linePaint);
        if (checked) {
            Paint tick = new Paint(Paint.ANTI_ALIAS_FLAG);
            tick.setStrokeWidth(1.6f);
            tick.setStyle(Paint.Style.STROKE);
            c.drawLine(x + 2, y + size * 0.55f, x + size * 0.42f, y + size - 2, tick);
            c.drawLine(x + size * 0.42f, y + size - 2, x + size - 2, y + 2, tick);
        }
    }

    private static boolean containsIgnoreCase(String haystack, String needle) {
        return haystack != null && needle != null &&
                haystack.toLowerCase(Locale.ENGLISH).contains(needle.toLowerCase(Locale.ENGLISH));
    }

    private static String fullAddress(JSONObject data) {
        String full = data.optString("fullAddress", "");
        if (!full.trim().isEmpty()) return full;

        StringBuilder sb = new StringBuilder();
        appendPart(sb, data.optString("village"));
        appendPart(sb, data.optString("panchayat"));
        appendPart(sb, data.optString("block"));
        appendPart(sb, data.optString("city"));
        appendPart(sb, data.optString("state"));
        return sb.toString();
    }

    private static void appendPart(StringBuilder sb, String s) {
        if (s == null || s.trim().isEmpty() || "Select".equalsIgnoreCase(s.trim())) return;
        if (sb.length() > 0) sb.append(", ");
        sb.append(s.trim());
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static String repeat(char c, int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(c);
        return sb.toString();
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
