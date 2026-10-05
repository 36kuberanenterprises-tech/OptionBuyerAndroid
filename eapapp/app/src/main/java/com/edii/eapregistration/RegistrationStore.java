package com.edii.eapregistration;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class RegistrationStore extends SQLiteOpenHelper {
    public static class Pending {
        public long id;
        public String requestId;
        public String payload;
        Pending(long id, String requestId, String payload) {
            this.id = id;
            this.requestId = requestId;
            this.payload = payload;
        }
    }

    public RegistrationStore(Context context) {
        super(context, "eap_offline.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE registrations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "request_id TEXT NOT NULL UNIQUE," +
                "payload TEXT NOT NULL," +
                "sync_status TEXT NOT NULL DEFAULT 'PENDING'," +
                "last_error TEXT," +
                "created_at INTEGER NOT NULL," +
                "synced_at INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public long savePending(String requestId, String payload) {
        ContentValues v = new ContentValues();
        v.put("request_id", requestId);
        v.put("payload", payload);
        v.put("sync_status", "PENDING");
        v.put("created_at", System.currentTimeMillis());
        return getWritableDatabase().insertOrThrow("registrations", null, v);
    }

    public List<Pending> getPending(int limit) {
        List<Pending> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query(
                "registrations",
                new String[]{"id","request_id","payload"},
                "sync_status<>?",
                new String[]{"SYNCED"},
                null, null, "created_at ASC", String.valueOf(limit));
        try {
            while (c.moveToNext()) {
                list.add(new Pending(c.getLong(0), c.getString(1), c.getString(2)));
            }
        } finally {
            c.close();
        }
        return list;
    }

    public void markSynced(long id) {
        ContentValues v = new ContentValues();
        v.put("sync_status", "SYNCED");
        v.put("synced_at", System.currentTimeMillis());
        v.putNull("last_error");
        getWritableDatabase().update("registrations", v, "id=?", new String[]{String.valueOf(id)});
    }

    public void markFailed(long id, String error) {
        ContentValues v = new ContentValues();
        v.put("sync_status", "PENDING");
        if (error != null && error.length() > 500) error = error.substring(0, 500);
        v.put("last_error", error == null ? "Sync error" : error);
        getWritableDatabase().update("registrations", v, "id=?", new String[]{String.valueOf(id)});
    }

    public int pendingCount() {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM registrations WHERE sync_status<>'SYNCED'", null);
        try {
            return c.moveToFirst() ? c.getInt(0) : 0;
        } finally {
            c.close();
        }
    }
}
