package com.edii.eapregistration;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyncJobService extends JobService {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean stopped = false;

    @Override
    public boolean onStartJob(JobParameters params) {
        stopped = false;
        executor.execute(() -> {
            boolean retry = false;
            try {
                SharedPreferences prefs =
                        getSharedPreferences("sync_settings", Context.MODE_PRIVATE);
                String apiUrl = prefs.getString("api_url", "");
                String apiToken = prefs.getString("api_token", "");

                if (apiUrl == null || !apiUrl.startsWith("https://") ||
                        apiToken == null || apiToken.trim().isEmpty()) {
                    jobFinished(params, false);
                    return;
                }

                RegistrationStore store = new RegistrationStore(this);
                List<RegistrationStore.Pending> pending = store.getPending(50);

                for (RegistrationStore.Pending item : pending) {
                    if (stopped) {
                        retry = true;
                        break;
                    }

                    try {
                        JSONObject json = new JSONObject(item.payload);
                        json.put("clientRequestId", item.requestId);

                        String totalSerial = json.optString("totalSerialNumber", "");
                        String locationSerial = json.optString("individualSerialNumber", "");

                        if (totalSerial.isEmpty() || locationSerial.isEmpty()) {
                            JSONObject reserve = new JSONObject();
                            reserve.put("action", "reserveSerial");
                            reserve.put("token", apiToken.trim());
                            reserve.put("clientRequestId", item.requestId);
                            reserve.put("projectLocation",
                                    json.optString("projectLocation", ""));
                            reserve.put("locationCode",
                                    json.optString("locationCode", ""));
                            reserve.put("name", json.optString("name", ""));

                            JSONObject reserveResult = post(apiUrl, reserve);
                            if (!reserveResult.optBoolean("success", false)) {
                                throw new Exception(reserveResult.optString(
                                        "message", "Unable to reserve central serial."));
                            }

                            json.put("totalSerialNumber",
                                    reserveResult.optString("totalSerialNumber", ""));
                            json.put("individualSerialNumber",
                                    reserveResult.optString("individualSerialNumber", ""));
                            json.put("totalApplicationCount",
                                    reserveResult.optInt("totalApplicationCount", 0));
                            json.put("locationApplicationCount",
                                    reserveResult.optInt("locationApplicationCount", 0));

                            totalSerial = json.optString("totalSerialNumber", "");
                            locationSerial = json.optString("individualSerialNumber", "");

                            if (totalSerial.isEmpty() || locationSerial.isEmpty()) {
                                throw new Exception("Central serial response is incomplete.");
                            }

                            store.updatePayload(item.id, json.toString());
                        }

                        if (json.optString("pdfBase64", "").isEmpty()) {
                            PdfGenerator.Result pdf =
                                    PdfGenerator.createAndSave(this, json, item.requestId);
                            json.put("pdfFileName", pdf.fileName);
                            json.put("pdfBase64", pdf.base64);
                            json.put("localPdfLocation", pdf.savedLocation);
                            store.updatePayload(item.id, json.toString());
                        }

                        json.put("action", "submitRegistration");
                        json.put("token", apiToken.trim());

                        JSONObject result = post(apiUrl, json);
                        if (result.optBoolean("success", false)) {
                            json.put("totalSerialNumber",
                                    result.optString(
                                            "totalSerialNumber",
                                            json.optString("totalSerialNumber", "")));
                            json.put("individualSerialNumber",
                                    result.optString(
                                            "individualSerialNumber",
                                            json.optString("individualSerialNumber", "")));
                            json.put("pdfUrl", result.optString("pdfUrl", ""));
                            json.put("photoUrl", result.optString("photoUrl", ""));
                            json.put("signatureUrl", result.optString("signatureUrl", ""));
                            json.put("editableUrl", result.optString("editableUrl", ""));
                            json.put("folderUrl", result.optString("folderUrl", ""));
                            json.put("totalApplicationCount",
                                    result.optInt(
                                            "totalApplicationCount",
                                            json.optInt("totalApplicationCount", 0)));
                            json.put("locationApplicationCount",
                                    result.optInt(
                                            "locationApplicationCount",
                                            json.optInt("locationApplicationCount", 0)));

                            store.updatePayload(item.id, json.toString());
                            store.markSynced(item.id);
                        } else {
                            store.markFailed(
                                    item.id,
                                    result.optString("message", "Server rejected record"));
                            retry = true;
                        }
                    } catch (Exception ex) {
                        store.markFailed(item.id, ex.getMessage());
                        retry = true;
                    }
                }

                if (store.pendingCount() > 0) {
                    retry = true;
                }
            } catch (Exception ex) {
                retry = true;
            }

            jobFinished(params, retry);
        });
        return true;
    }

    private JSONObject post(String apiUrl, JSONObject json) throws Exception {
        String body = "payload=" +
                URLEncoder.encode(json.toString(), "UTF-8");
        byte[] data = body.getBytes(StandardCharsets.UTF_8);

        HttpURLConnection conn =
                (HttpURLConnection) new URL(apiUrl.trim()).openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(20000);
        conn.setReadTimeout(40000);
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(true);
        conn.setRequestProperty(
                "Content-Type",
                "application/x-www-form-urlencoded; charset=UTF-8");
        conn.setFixedLengthStreamingMode(data.length);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(data);
        }

        int code = conn.getResponseCode();
        InputStream stream = code >= 200 && code < 400
                ? conn.getInputStream()
                : conn.getErrorStream();

        String response = readAll(stream);
        conn.disconnect();

        if (code < 200 || code >= 400) {
            throw new Exception("HTTP " + code);
        }

        return new JSONObject(response);
    }

    private String readAll(InputStream stream) throws Exception {
        if (stream == null) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br =
                     new BufferedReader(
                             new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        stopped = true;
        return true;
    }
}
