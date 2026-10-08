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
                SharedPreferences prefs = getSharedPreferences("sync_settings", Context.MODE_PRIVATE);
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
                        json.put("action", "submitRegistration");
                        json.put("token", apiToken.trim());
                        json.put("clientRequestId", item.requestId);

                        String body = "payload=" + URLEncoder.encode(json.toString(), "UTF-8");
                        byte[] data = body.getBytes(StandardCharsets.UTF_8);

                        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl.trim()).openConnection();
                        conn.setRequestMethod("POST");
                        conn.setConnectTimeout(20000);
                        conn.setReadTimeout(30000);
                        conn.setDoOutput(true);
                        conn.setInstanceFollowRedirects(true);
                        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                        conn.setFixedLengthStreamingMode(data.length);

                        try (OutputStream os = conn.getOutputStream()) {
                            os.write(data);
                        }

                        int code = conn.getResponseCode();
                        InputStream stream = code >= 200 && code < 400 ?
                                conn.getInputStream() : conn.getErrorStream();
                        String response = readAll(stream);
                        conn.disconnect();

                        if (code >= 200 && code < 400) {
                            JSONObject result = new JSONObject(response);
                            if (result.optBoolean("success", false)) {
                                store.markSynced(item.id);
                            } else {
                                store.markFailed(item.id, result.optString("message", "Server rejected record"));
                                retry = true;
                            }
                        } else {
                            store.markFailed(item.id, "HTTP " + code);
                            retry = true;
                        }
                    } catch (Exception ex) {
                        store.markFailed(item.id, ex.getMessage());
                        retry = true;
                    }
                }
            } catch (Exception ex) {
                retry = true;
            }
            jobFinished(params, retry);
        });
        return true;
    }

    private String readAll(InputStream stream) throws Exception {
        if (stream == null) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
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
