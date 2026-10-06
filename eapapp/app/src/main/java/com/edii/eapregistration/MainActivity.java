package com.edii.eapregistration;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends Activity {
    private static final int PHOTO_REQUEST = 2001;
    private static final int SYNC_JOB_ID = 31001;

    private LinearLayout container;
    private ImageView photoPreview;
    private EditText eapDateField;
    private EditText dobField;
    private EditText ageField;
    private String photoBase64 = "";
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sdf.setLenient(false);

        ScrollView scroll = new ScrollView(this);
        container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(18), dp(14), dp(18), dp(36));
        scroll.addView(container, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT));
        setContentView(scroll);

        addTitle("EAP Registration Form");
        addSyncSettings();

        eapDateField = addDateField("EAP Date *");
        addPhotoSection();

        EditText name = addTextField("Name *", "");
        RadioGroup gender = addRadioGroup("Gender *", new String[]{"Male", "Female", "Other"});

        dobField = addDateField("Date of Birth *");
        EditText guardianName = addTextField("Father's / Husband's / Mother's Name *", "");
        ageField = addTextField("Age on EAP Date", "");
        ageField.setEnabled(false);

        addSection("Address");
        EditText village = addTextField("Village *", "");
        EditText panchayat = addTextField("Panchayat *", "");
        EditText block = addTextField("Block", "");
        Spinner city = addSpinner("City *", new String[]{"Select", "Mulbagal", "Srinivaspur"});
        EditText state = addTextField("State *", "Karnataka");

        EditText mobile = addTextField("Mobile Number *", "");
        mobile.setInputType(InputType.TYPE_CLASS_PHONE);
        EditText alternateMobile = addTextField("Alternate Mobile Number", "");
        alternateMobile.setInputType(InputType.TYPE_CLASS_PHONE);
        EditText email = addTextField("Email ID", "");
        email.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        EditText idNumber = addTextField("Aadhaar No.", "");
        idNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        addNote("Collect Aadhaar details only as approved by the organisation and applicable data protection requirements.");

        EditText education = addTextField("Highest Educational Qualification *", "");
        EditText occupation = addTextField("Occupation *", "");
        EditText income = addTextField("Individual Income", "");
        income.setInputType(InputType.TYPE_CLASS_NUMBER);

        Spinner category = addSpinner("Category *",
                new String[]{"Select", "SC", "ST", "OBC"});

        RadioGroup intention = addRadioGroup(
                "Intention for taking part in EAP *",
                new String[]{"Employment", "Self Employment"});

        List<CheckBox> sectors = addCheckGroup(
                "Sector in which you would like to start business *",
                new String[]{"Fashion Technology", "Food Processing", "Jute Bag Manufacturing", "Beautician"});

        RadioGroup msdp = addRadioGroup(
                "Do you want to attend MSDP to understand business? *",
                new String[]{"Yes", "No"});

        CheckBox declaration = new CheckBox(this);
        declaration.setText("I declare that the information provided by me is correct. I will be responsible for any discrepancy detected.");
        declaration.setPadding(0, dp(12), 0, dp(8));
        container.addView(declaration);

        Button submit = new Button(this);
        submit.setText("SUBMIT REGISTRATION");
        container.addView(submit);

        TextView status = new TextView(this);
        status.setPadding(0, dp(12), 0, 0);
        container.addView(status);

        submit.setOnClickListener(v -> {
            List<String> selectedSectors = new ArrayList<>();
            for (CheckBox cb : sectors) if (cb.isChecked()) selectedSectors.add(cb.getText().toString());

            if (blank(eapDateField) || blank(name) || selectedRadio(gender).isEmpty() ||
                    blank(dobField) || blank(guardianName) || blank(village) || blank(panchayat) ||
                    "Select".equals(selectedSpinner(city)) || blank(state) || blank(mobile) ||
                    blank(education) || blank(occupation) ||
                    "Select".equals(selectedSpinner(category)) ||
                    selectedRadio(intention).isEmpty() || selectedRadio(msdp).isEmpty() ||
                    selectedSectors.isEmpty() || !declaration.isChecked()) {
                Toast.makeText(this, "Please complete all mandatory fields and declaration.", Toast.LENGTH_LONG).show();
                return;
            }

            String mobileText = mobile.getText().toString().trim();
            if (!mobileText.matches("^[6-9][0-9]{9}$")) {
                Toast.makeText(this, "Enter a valid 10 digit mobile number.", Toast.LENGTH_LONG).show();
                return;
            }

            try {
                JSONObject payload = new JSONObject();
                payload.put("eapDate", value(eapDateField));
                payload.put("name", value(name));
                payload.put("gender", selectedRadio(gender));
                payload.put("dob", value(dobField));
                payload.put("guardianName", value(guardianName));
                payload.put("ageOnEapDate", value(ageField));
                payload.put("village", value(village));
                payload.put("panchayat", value(panchayat));
                payload.put("block", value(block));
                payload.put("city", selectedSpinner(city));
                payload.put("state", value(state));
                payload.put("place", selectedSpinner(city));
                payload.put("fullAddress", buildFullAddress(value(village), value(panchayat), value(block), selectedSpinner(city), value(state)));
                payload.put("mobile", mobileText);
                payload.put("alternateMobile", value(alternateMobile));
                payload.put("email", value(email));
                payload.put("idType", "Aadhaar");
                payload.put("idNumber", value(idNumber));
                payload.put("education", value(education));
                payload.put("occupation", value(occupation));
                payload.put("individualIncome", value(income));
                payload.put("category", selectedSpinner(category));
                payload.put("intention", selectedRadio(intention));
                payload.put("sectors", join(selectedSectors));
                payload.put("msdpInterest", selectedRadio(msdp));
                payload.put("photoBase64", photoBase64);
                payload.put("declarationAccepted", true);

                String requestId = UUID.randomUUID().toString();
                payload.put("clientRequestId", requestId);

                PdfGenerator.Result pdf = PdfGenerator.createAndSave(this, payload, requestId);
                payload.put("pdfFileName", pdf.fileName);
                payload.put("pdfBase64", pdf.base64);

                RegistrationStore store = new RegistrationStore(this);
                store.savePending(requestId, payload.toString());
                scheduleSync();

                int pending = store.pendingCount();
                status.setText("Registration saved. PDF: " + pdf.savedLocation +
                        ". Pending Google Drive sync: " + pending +
                        ". It will sync automatically when internet is available.");
                Toast.makeText(this, "Registration and PDF saved.", Toast.LENGTH_LONG).show();
            } catch (Exception ex) {
                status.setText("Unable to save: " + ex.getMessage());
                Toast.makeText(this, "Unable to save registration.", Toast.LENGTH_LONG).show();
            }
        });

        if (new RegistrationStore(this).pendingCount() > 0) scheduleSync();
    }

    private void addSyncSettings() {
        addSection("Google Sync Settings");
        SharedPreferences prefs = getSharedPreferences("sync_settings", MODE_PRIVATE);

        EditText url = addTextField("Apps Script Web App URL", prefs.getString("api_url", ""));
        EditText token = addTextField("API Token", prefs.getString("api_token", ""));
        token.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        Button save = new Button(this);
        save.setText("SAVE SETTINGS");
        Button sync = new Button(this);
        sync.setText("SYNC NOW");
        row.addView(save, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(sync, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        container.addView(row);

        TextView pendingView = new TextView(this);
        pendingView.setPadding(0, dp(6), 0, dp(10));
        container.addView(pendingView);
        refreshPending(pendingView);

        save.setOnClickListener(v -> {
            String u = value(url);
            String t = value(token);
            if (!u.isEmpty() && !u.startsWith("https://")) {
                Toast.makeText(this, "Enter a valid HTTPS Apps Script URL.", Toast.LENGTH_LONG).show();
                return;
            }
            prefs.edit().putString("api_url", u).putString("api_token", t).apply();
            scheduleSync();
            refreshPending(pendingView);
            Toast.makeText(this, "Sync settings saved.", Toast.LENGTH_SHORT).show();
        });

        sync.setOnClickListener(v -> {
            scheduleSync();
            refreshPending(pendingView);
            Toast.makeText(this, "Sync queued. It will run when internet is available.", Toast.LENGTH_SHORT).show();
        });
    }

    private void refreshPending(TextView view) {
        int count = new RegistrationStore(this).pendingCount();
        view.setText("Pending sync: " + count + ". Automatic sync is enabled.");
    }

    private void scheduleSync() {
        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo info = new JobInfo.Builder(
                SYNC_JOB_ID,
                new ComponentName(this, SyncJobService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setBackoffCriteria(30000, JobInfo.BACKOFF_POLICY_LINEAR)
                .build();
        scheduler.schedule(info);
    }

    private void addTitle(String text) {
        TextView title = new TextView(this);
        title.setText(text);
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(8), 0, dp(12));
        container.addView(title);
    }

    private void addSection(String text) {
        TextView title = new TextView(this);
        title.setText(text);
        title.setTextSize(18);
        title.setPadding(0, dp(16), 0, dp(6));
        container.addView(title);
    }

    private void addNote(String text) {
        TextView note = new TextView(this);
        note.setText(text);
        note.setTextSize(12);
        note.setPadding(0, 0, 0, dp(8));
        container.addView(note);
    }

    private EditText addTextField(String label, String initialValue) {
        TextView l = new TextView(this);
        l.setText(label);
        l.setPadding(0, dp(7), 0, dp(2));
        container.addView(l);

        EditText e = new EditText(this);
        e.setSingleLine(true);
        e.setText(initialValue == null ? "" : initialValue);
        container.addView(e, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        return e;
    }

    private EditText addDateField(String label) {
        EditText e = addTextField(label, "");
        e.setFocusable(false);
        e.setClickable(true);
        e.setOnClickListener(v -> showDatePicker(e));
        return e;
    }

    private Spinner addSpinner(String label, String[] items) {
        TextView l = new TextView(this);
        l.setText(label);
        l.setPadding(0, dp(10), 0, dp(3));
        container.addView(l);

        Spinner s = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, items);
        s.setAdapter(adapter);
        container.addView(s);
        return s;
    }

    private RadioGroup addRadioGroup(String label, String[] items) {
        TextView l = new TextView(this);
        l.setText(label);
        l.setPadding(0, dp(10), 0, dp(3));
        container.addView(l);

        RadioGroup g = new RadioGroup(this);
        g.setOrientation(RadioGroup.VERTICAL);
        for (String item : items) {
            RadioButton rb = new RadioButton(this);
            rb.setText(item);
            g.addView(rb);
        }
        container.addView(g);
        return g;
    }

    private List<CheckBox> addCheckGroup(String label, String[] items) {
        TextView l = new TextView(this);
        l.setText(label);
        l.setPadding(0, dp(10), 0, dp(3));
        container.addView(l);

        List<CheckBox> list = new ArrayList<>();
        for (String item : items) {
            CheckBox cb = new CheckBox(this);
            cb.setText(item);
            container.addView(cb);
            list.add(cb);
        }
        return list;
    }

    private void addPhotoSection() {
        addSection("Photo");
        photoPreview = new ImageView(this);
        photoPreview.setBackgroundColor(0xFFECECEC);
        photoPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        container.addView(photoPreview, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(220)));

        Button photo = new Button(this);
        photo.setText("TAKE PHOTO");
        container.addView(photo);
        photo.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, PHOTO_REQUEST);
            } else {
                Toast.makeText(this, "Camera is not available.", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_REQUEST && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            Object obj = extras == null ? null : extras.get("data");
            if (obj instanceof Bitmap) {
                Bitmap bitmap = (Bitmap) obj;
                photoPreview.setImageBitmap(bitmap);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, out);
                photoBase64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
            }
        }
    }

    private void showDatePicker(EditText target) {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, day, 0, 0, 0);
                    target.setText(sdf.format(selected.getTime()));
                    updateAge();
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void updateAge() {
        if (dobField == null || eapDateField == null || ageField == null) return;
        String dobText = value(dobField);
        String eventText = value(eapDateField);
        if (dobText.isEmpty() || eventText.isEmpty()) return;

        try {
            Calendar dob = Calendar.getInstance();
            dob.setTime(sdf.parse(dobText));
            Calendar event = Calendar.getInstance();
            event.setTime(sdf.parse(eventText));

            int age = event.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
            boolean birthdayPassed =
                    event.get(Calendar.MONTH) > dob.get(Calendar.MONTH) ||
                    (event.get(Calendar.MONTH) == dob.get(Calendar.MONTH) &&
                            event.get(Calendar.DAY_OF_MONTH) >= dob.get(Calendar.DAY_OF_MONTH));
            if (!birthdayPassed) age--;
            ageField.setText(age >= 0 ? String.valueOf(age) : "");
        } catch (Exception ignored) {}
    }

    private String selectedRadio(RadioGroup group) {
        int id = group.getCheckedRadioButtonId();
        if (id == -1) return "";
        RadioButton rb = findViewById(id);
        return rb == null ? "" : rb.getText().toString();
    }

    private String selectedSpinner(Spinner spinner) {
        Object item = spinner.getSelectedItem();
        return item == null ? "" : item.toString();
    }

    private boolean blank(EditText e) {
        return value(e).isEmpty();
    }

    private String value(EditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private String buildFullAddress(String village, String panchayat, String block, String city, String state) {
        List<String> parts = new ArrayList<>();
        if (!village.isEmpty()) parts.add(village);
        if (!panchayat.isEmpty()) parts.add(panchayat);
        if (!block.isEmpty()) parts.add(block);
        if (!city.isEmpty() && !"Select".equals(city)) parts.add(city);
        if (!state.isEmpty()) parts.add(state);
        return join(parts);
    }

    private String join(List<String> values) {
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(value);
        }
        return sb.toString();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
