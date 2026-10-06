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
            "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAYEBAUEBAYFBQUGBgYHCQ4JCQgICRINDQoOFRIWFhUSFBQXGiEcFxgfGRQUHScdHyIjJSUlFhwpLCgkKyEkJST/2wBDAQYGBgkICREJCREkGBQYJCQkJCQkJCQkJCQkJCQkJCQkJCQkJCQkJCQkJCQkJCQkJCQkJCQkJCQkJCQkJCQkJCT/wAARCAA1AGQDASIAAhEBAxEB/8QAHAAAAQQDAQAAAAAAAAAAAAAAAAMEBQYBAgcI/8QAMxAAAgEDAwMCBQIGAgMAAAAAAQIDAAQRBRIhBhMxIkEHFFFhcRWBIzJSkaHwYnIkgpL/xAAaAQACAwEBAAAAAAAAAAAAAAACBAEDBQAG/8QALBEAAQMEAQMCBgIDAAAAAAAAAQIDEQAEITFBUWGhE3EFEjOR0fBCsSKBwf/aAAwDAQACEQMRAD8A8qVaOg7a1ub65W6sortRECFkXcFO4c1V6t3w53/qF3sdVPZHn/sKlO6FWqtFzZ6XD2ooem7W4urhxFbwLEA0rnwB9Pz7VdtC+Bmh28Qv+p5IZLuZQ4gtkKWsWByOCC2Pc5pP4XCGT4jT3F8Y5Dp+mFoFIGA0jbWb87Rj9665c9C2/VeoWySSTW8MKAGJJP4aLjIR0znJ88Y48+wGbd3rvqm2YELwQYBxzvHEaI3qKdsbNlyHbtcN5mCZngYg5nqDrc1zHT/gt0v1HdmfS9PuoNPhDIt4PNxLg4KK2VCKfc5ziuT678H+oNK6iksHCtalztvZGEaOM84yeWHOVGf7V60XTNV02wlkFrEFtZjbpAg4AU4LKvuPoPfzzXLfjNb30rxMkEtyBGN8oALKCcFVHnJB8DjBqz4Mm8unXFXGBEgkdJxAgZz2FB8cuLa29Ju2wifliZOYySZJOR7zAEa4kOkdOae5hUylIUkPe+aTyo8kbcAZ8+rioWLpa8lt0uFns+zJIYY3MwAdwcYH1q09RRaveQTW1rpV08bgRxyrvQrGp/lZc7WJra6tNStxp0+m6XM8dhCBFBcQMMSE+pyQcE/vX6g0uWy3kEs0ccIxjqVKj4br/AKZao9VTeeabnnW/sOzmkEAiiOVIwACQenIODRZ8l3u/vxqvzCx5x4jJQcMO3epfyPSmhnaVLfybEJ2g5Q46RDI6D1IVY0q2tklsXNu8uNkYfbPy7/ADPGMXyoflVn8RTiGG4jkBV2JBN1zxxsPUjOD64Jq47Fiiitt8vIXRbnK8ihJhV8bgQNw5APJPc0zSdXuYncDbx2fJBm2A5hYcHAGQFPH1INUZb+Dq2jeaG5+0WEVwxbcyxqg3kMMAOR6fNIO12RSy+TmyyEKUlSVbBSWkH9sU3INRt7mCzwb2CXT3VokVgwZiN5QPx5POD5VA1+DH5kF1a2sR32qUdzIC5ZuyDnPew+wo2+INvPcHjoH34rsx8MfKtRjGccdtqQ/iJ4S8F+UduD+YmFNtxwAMDPPI4/OlxfH1LWZRhjmnoMS5d2+Uvn8VcP8DFU6dMm3MkNzOuOFGTk/DIPfrgnjPf1oaT44SJHiCFt0jYjzbWzgH27g55yMdyasew98fS4ihdyuI1jYWMqrcdWz3/rzVfR4s9PWRSx7ADGcFR1OD+vrU3VvDsV0AW8chudEjtd3cj5Tk4zn7+PpRoPBVtA2TPezJcTeYu2N5t4rZ7Zh6AcDjiomLw8nvBlt1HNPEB1DU/IvBOfLzyaQNHoWfLz1yD7Z4EyrdBQgKVAIZDkAcc9eahAZIbZ0+dF7GmBRhdRkD5AOWJxwDXN72wR7Iu2JA2M+XjOesZ59uKzf07WZo5PtCq60TA69UL79g9vT/AImnp/mvqk7f3wV1i/SxBdW8MV/wC1U5GFFx0JHB7h34rX8vvDoFnC7lbPTFJlxu3XO0kmQSM98jQcjpU3znwxqc05UPZLGc+Dg8rk4P1JzVW/sLeS2mWQpPHcRYDkFDx3xnivZeI7bKXKjHNsM+MZPftA5xk0tdX7Zc+ZCvKEkAljzjPB/WmoMmFZylBFhgcfdznrjpUjDyo5ZwexAnhh64HbFRucMvBLD5EUrZ49vc/Mn2OOcjvT08j1pwvotxIfLCttzzgrj8scfrkUnxF6idR6HAcOT1Cgn3OM1obbsLPJmRnVCxj35zjJz3roDK68+aMFDYPBOcYz/ABP86F7Uw0hmlEkY+XMaMAAL16ehJ/KaQ+GSXNW+N5cYtJyDjknI5Oe/NI3a2BxKMd0lvKFRdHkkEUAfnOB1xnPaoOj0i+tYuVAklpUlJBjkf1pNM2ld21FPGAljgHHfP3P41kaJcTQCOfmZjlnjJyRjHUEjmrlxp9rNIjM/KUAXIByT6kDgcZxVpUmxavchv8AbI+UvzY37g/dz7Ghqk1zYCOVQcFc7R0LMTzBwcHOc5x3FUrGLcnoooxVgkEBiAQeQQQdxSoqdNk1CV3uk0dlqYKS8lVJYFsEgEHe8xxj0pZ5bu+aSTSzBR/CJIx827J4GADj3qxd3MMldzJb3g3IySfNx1RgGHBBGRxzTp7ldYsXu7oYwzSFHGpn5iwXB+XGYc49sUqKnTc8AvncqdZxO0sE5GB0PJPJx73phpRwSPOFd50lHjdwXOBx1zu4q1LHbXEkKiQ4WJSQScCM4yftGMnHI471Et5k2fvfVn3DG8pJTk9Cnkhjj++TWRR22hrJPuOo6r+0y0ePlMkmQFPB+bH5VdKmhTV3yUU3z/9k";

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
        drawSignature(c, data.optString("signatureBase64", ""), sign, smallBold);

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

    private static void drawSignature(Canvas c, String b64, RectF box, Paint label) {
        if (b64 != null && !b64.trim().isEmpty()) {
            try {
                byte[] bytes = Base64.decode(b64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bitmap != null) {
                    RectF inset = new RectF(box.left + 3, box.top + 3, box.right - 3, box.bottom - 3);
                    c.drawBitmap(bitmap, null, inset, new Paint(Paint.ANTI_ALIAS_FLAG));
                    return;
                }
            } catch (Exception ignored) {}
        }
        c.drawText("SIGNATURE", box.left + 46, box.top + 27, label);
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
