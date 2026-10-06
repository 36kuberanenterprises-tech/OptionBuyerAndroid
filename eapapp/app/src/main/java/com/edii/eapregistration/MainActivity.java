package com.edii.eapregistration;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends Activity {

    private static final int PHOTO_REQUEST = 2001;
    private static final int SIGNATURE_PHOTO_REQUEST = 2002;
    private static final int PHOTO_PICK_REQUEST = 2003;
    private static final int SIGNATURE_PICK_REQUEST = 2004;
    private static final int SYNC_JOB_ID = 31001;

    private static final int GREEN = Color.rgb(40, 153, 50);
    private static final int GREEN_DARK = Color.rgb(28, 125, 39);
    private static final int GREEN_LIGHT = Color.rgb(236, 248, 236);
    private static final int PAGE_BG = Color.rgb(247, 250, 247);
    private static final int TEXT = Color.rgb(30, 37, 32);
    private static final int MUTED = Color.rgb(103, 112, 106);
    private static final int BORDER = Color.rgb(218, 226, 219);

    private FrameLayout contentFrame;
    private TextView navHome;
    private TextView navApplications;
    private TextView navMore;
    private ScrollView homeScroll;
    private LinearLayout homeContainer;

    private ImageView photoPreview;
    private ImageView signaturePhotoPreview;
    private String photoBase64 = "";
    private String signaturePhotoBase64 = "";

    private EditText eapDateField;
    private EditText nameField;
    private EditText dobField;
    private EditText ageField;
    private EditText guardianNameField;
    private EditText villageField;
    private EditText panchayatField;
    private EditText cityField;
    private EditText pinCodeField;
    private EditText stateField;
    private EditText mobileField;
    private EditText alternateMobileField;
    private EditText emailField;
    private EditText aadhaarField;
    private EditText educationField;
    private EditText occupationField;
    private EditText incomeField;

    private Spinner projectLocationSpinner;
    private Spinner categorySpinner;
    private RadioGroup genderGroup;
    private RadioGroup intentionGroup;
    private RadioGroup sectorGroup;
    private RadioGroup msdpGroup;
    private CheckBox declarationCheck;

    private TextView totalSerialView;
    private TextView locationSerialView;
    private TextView saveStatusView;
    private Button submitButton;

    private Calendar eapDateValue = Calendar.getInstance();
    private Calendar dobValue = null;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
    private final SimpleDateFormat historyDateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sdf.setLenient(false);
        buildAppShell();
        showHome();

        if (new RegistrationStore(this).pendingCount() > 0) {
            scheduleSync();
        }
    }

    private void buildAppShell() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(PAGE_BG);

        root.addView(createHeader(), new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(70)));

        contentFrame = new FrameLayout(this);
        root.addView(contentFrame, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        root.addView(createBottomNavigation(), new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(64)));

        setContentView(root);
    }

    private View createHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16), dp(8), dp(16), dp(8));
        header.setBackground(gradient(GREEN_DARK, GREEN, 0));

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.ic_edii_app);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        header.addView(logo, new LinearLayout.LayoutParams(dp(48), dp(48)));

        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        titles.setPadding(dp(10), 0, 0, 0);

        TextView brand = new TextView(this);
        brand.setText("EDII");
        brand.setTextColor(Color.WHITE);
        brand.setTextSize(18);
        brand.setTypeface(Typeface.DEFAULT_BOLD);

        TextView title = new TextView(this);
        title.setText("HAL EAP Registration");
        title.setTextColor(Color.WHITE);
        title.setTextSize(15);
        title.setTypeface(Typeface.DEFAULT_BOLD);

        titles.addView(brand);
        titles.addView(title);
        header.addView(titles, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView syncBadge = new TextView(this);
        syncBadge.setText("●");
        syncBadge.setTextColor(Color.WHITE);
        syncBadge.setTextSize(20);
        syncBadge.setGravity(Gravity.CENTER);
        syncBadge.setContentDescription("Sync status");
        syncBadge.setOnClickListener(v -> {
            scheduleSync();
            Toast.makeText(this, "Sync queued.", Toast.LENGTH_SHORT).show();
        });
        header.addView(syncBadge, new LinearLayout.LayoutParams(dp(44), dp(44)));

        return header;
    }

    private View createBottomNavigation() {
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(8), dp(5), dp(8), dp(5));
        nav.setBackgroundColor(Color.WHITE);
        nav.setElevation(dp(8));

        navHome = navItem("Home");
        navApplications = navItem("Applications");
        navMore = navItem("More");

        nav.addView(navHome, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1f));
        nav.addView(navApplications, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1f));
        nav.addView(navMore, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1f));

        navHome.setOnClickListener(v -> showHome());
        navApplications.setOnClickListener(v -> showApplications());
        navMore.setOnClickListener(v -> showMore());

        return nav;
    }

    private TextView navItem(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setGravity(Gravity.CENTER);
        t.setTextSize(13);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setPadding(dp(4), dp(4), dp(4), dp(4));
        return t;
    }

    private void setActiveNav(TextView active) {
        TextView[] items = {navHome, navApplications, navMore};
        for (TextView item : items) {
            item.setTextColor(item == active ? GREEN_DARK : MUTED);
            item.setBackground(item == active
                    ? rounded(GREEN_LIGHT, dp(14), 0, 0)
                    : rounded(Color.TRANSPARENT, dp(14), 0, 0));
        }
    }

    private void showHome() {
        setActiveNav(navHome);
        if (homeScroll == null) {
            buildHomeScreen();
        }
        attachContent(homeScroll);
        updateSerialPreview();
    }

    private void buildHomeScreen() {
        homeScroll = new ScrollView(this);
        homeScroll.setFillViewport(true);

        homeContainer = new LinearLayout(this);
        homeContainer.setOrientation(LinearLayout.VERTICAL);
        homeContainer.setPadding(dp(14), dp(12), dp(14), dp(24));
        homeScroll.addView(homeContainer, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT));

        TextView screenTitle = new TextView(this);
        screenTitle.setText("New EAP Registration");
        screenTitle.setTextColor(TEXT);
        screenTitle.setTextSize(22);
        screenTitle.setTypeface(Typeface.DEFAULT_BOLD);
        screenTitle.setPadding(dp(2), dp(2), 0, dp(10));
        homeContainer.addView(screenTitle);

        LinearLayout eventCard = createSectionCard(homeContainer, "Event Setup");

        eapDateField = createInput("");
        eapDateField.setFocusable(false);
        eapDateField.setClickable(true);
        eapDateField.setOnClickListener(v -> showDatePicker(eapDateField));

        eapDateValue = Calendar.getInstance();
        eapDateValue.set(Calendar.HOUR_OF_DAY, 0);
        eapDateValue.set(Calendar.MINUTE, 0);
        eapDateValue.set(Calendar.SECOND, 0);
        eapDateValue.set(Calendar.MILLISECOND, 0);
        eapDateField.setText(sdf.format(eapDateValue.getTime()));

        projectLocationSpinner = createSpinner(new String[]{
                "Select",
                "Kolar | Mulbagal",
                "Kolar | Srinivaspur",
                "Bengaluru Rural | Devanahalli",
                "Bengaluru Rural | Hoskote",
                "Tumkur | Tumkur",
                "Tumkur | Gubbi",
                "Tumkur | Sira",
                "Bengaluru South | Ramanagara",
                "Bengaluru South | Channapatna"
        });

        addPair(eventCard,
                fieldBlock("EAP Date *", eapDateField),
                fieldBlock("HAL Project Location *", projectLocationSpinner));

        addSummaryCards(homeContainer);

        LinearLayout personal = createSectionCard(homeContainer, "Personal Details");
        nameField = createInput("");
        genderGroup = createRadioGroup(new String[]{"Male", "Female", "Other"}, true);
        dobField = createInput("");
        dobField.setFocusable(false);
        dobField.setClickable(true);
        dobField.setOnClickListener(v -> showDatePicker(dobField));
        ageField = createInput("");
        ageField.setFocusable(false);
        ageField.setClickable(false);
        ageField.setTextColor(TEXT);
        ageField.setBackground(rounded(Color.rgb(242, 245, 242), dp(10), 1, BORDER));
        guardianNameField = createInput("");

        addPair(personal,
                fieldBlock("Name *", nameField),
                fieldBlock("Gender *", genderGroup));
        addPair(personal,
                fieldBlock("Date of Birth *", dobField),
                fieldBlock("Age (Auto)", ageField));
        personal.addView(fieldBlock("Father / Husband / Mother Name *", guardianNameField));

        LinearLayout location = createSectionCard(homeContainer, "Location Details");
        villageField = createInput("");
        panchayatField = createInput("");
        cityField = createInput("");
        cityField.setFocusable(false);
        cityField.setClickable(false);
        cityField.setTextColor(TEXT);
        cityField.setBackground(rounded(Color.rgb(242, 245, 242), dp(10), 1, BORDER));
        pinCodeField = createInput("");
        pinCodeField.setInputType(InputType.TYPE_CLASS_NUMBER);
        pinCodeField.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        stateField = createInput("Karnataka");
        stateField.setFocusable(false);
        stateField.setClickable(false);
        stateField.setBackground(rounded(Color.rgb(242, 245, 242), dp(10), 1, BORDER));

        addPair(location,
                fieldBlock("Village *", villageField),
                fieldBlock("Panchayat *", panchayatField));
        addPair(location,
                fieldBlock("City / Taluk (Auto)", cityField),
                fieldBlock("PIN Code *", pinCodeField));
        location.addView(fieldBlock("State", stateField));

        projectLocationSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selected = selectedSpinner(projectLocationSpinner);
                cityField.setText("Select".equals(selected) ? "" : projectPlace(selected));
                updateSerialPreview();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                updateSerialPreview();
            }
        });

        LinearLayout contact = createSectionCard(homeContainer, "Contact Details");
        mobileField = createInput("");
        mobileField.setInputType(InputType.TYPE_CLASS_PHONE);
        mobileField.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

        alternateMobileField = createInput("");
        alternateMobileField.setInputType(InputType.TYPE_CLASS_PHONE);
        alternateMobileField.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

        emailField = createInput("");
        emailField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        aadhaarField = createInput("");
        aadhaarField.setInputType(InputType.TYPE_CLASS_NUMBER);
        aadhaarField.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});

        addPair(contact,
                fieldBlock("Mobile Number *", mobileField),
                fieldBlock("Alternate Mobile Number", alternateMobileField));
        contact.addView(fieldBlock("Email ID", emailField));
        contact.addView(fieldBlock("Aadhaar No. (12 digits)", aadhaarField));

        LinearLayout education = createSectionCard(homeContainer, "Education & Employment Details");
        educationField = createInput("");
        occupationField = createInput("");
        incomeField = createInput("");
        incomeField.setInputType(InputType.TYPE_CLASS_NUMBER);
        categorySpinner = createSpinner(new String[]{
                "Select", "GEN", "EWS", "SC", "ST", "OBC", "MINORITY"
        });

        addPair(education,
                fieldBlock("Highest Educational Qualification *", educationField),
                fieldBlock("Occupation *", occupationField));
        addPair(education,
                fieldBlock("Individual Income", incomeField),
                fieldBlock("Category *", categorySpinner));

        LinearLayout eap = createSectionCard(homeContainer, "EAP Details");
        intentionGroup = createRadioGroup(new String[]{"Employment", "Self Employment"}, false);
        sectorGroup = createRadioGroup(new String[]{
                "Fashion Technology",
                "Food Processing",
                "Jute Bag Manufacturing",
                "Beautician"
        }, false);
        msdpGroup = createRadioGroup(new String[]{"Yes", "No"}, true);

        eap.addView(fieldBlock("Intention for taking part in EAP *", intentionGroup));
        eap.addView(fieldBlock("Business Sector *", sectorGroup));
        eap.addView(fieldBlock("Do you want to attend MSDP? *", msdpGroup));

        LinearLayout documents = createSectionCard(homeContainer, "Documents");
        documents.addView(buildCandidatePhotoBlock());
        documents.addView(divider());
        documents.addView(buildSignatureBlock());

        declarationCheck = new CheckBox(this);
        declarationCheck.setText("I declare that the information provided by me is correct. I will be responsible for any discrepancy detected.");
        declarationCheck.setTextColor(TEXT);
        declarationCheck.setTextSize(14);
        declarationCheck.setPadding(dp(4), dp(8), dp(4), dp(8));
        homeContainer.addView(declarationCheck, cardParams(dp(4), dp(8)));

        submitButton = makeButton("Submit Application", true);
        submitButton.setTextSize(17);
        submitButton.setOnClickListener(v -> submitRegistration());
        homeContainer.addView(submitButton, cardParams(dp(4), dp(8)));

        saveStatusView = new TextView(this);
        saveStatusView.setTextColor(MUTED);
        saveStatusView.setTextSize(12);
        saveStatusView.setPadding(dp(6), dp(6), dp(6), dp(12));
        homeContainer.addView(saveStatusView);
    }

    private void addSummaryCards(LinearLayout parent) {
        LinearLayout row = new LinearLayout(this);
        boolean wide = getResources().getConfiguration().screenWidthDp >= 420;
        row.setOrientation(wide ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);

        totalSerialView = summaryCard();
        locationSerialView = summaryCard();

        if (wide) {
            LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            lp1.setMargins(0, dp(4), dp(5), dp(8));
            row.addView(totalSerialView, lp1);

            LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            lp2.setMargins(dp(5), dp(4), 0, dp(8));
            row.addView(locationSerialView, lp2);
        } else {
            row.addView(totalSerialView, cardParams(0, dp(5)));
            row.addView(locationSerialView, cardParams(0, dp(5)));
        }

        parent.addView(row);
        updateSerialPreview();
    }

    private TextView summaryCard() {
        TextView card = new TextView(this);
        card.setTextColor(TEXT);
        card.setTextSize(14);
        card.setLineSpacing(0, 1.08f);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackground(rounded(GREEN_LIGHT, dp(16), 1, Color.rgb(215, 237, 215)));
        return card;
    }

    private View buildCandidatePhotoBlock() {
        LinearLayout block = new LinearLayout(this);
        block.setOrientation(LinearLayout.VERTICAL);

        TextView label = label("Candidate Photo *");
        block.addView(label);

        photoPreview = new ImageView(this);
        photoPreview.setBackground(rounded(Color.rgb(242, 245, 242), dp(12), 1, BORDER));
        photoPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        photoPreview.setAdjustViewBounds(true);
        block.addView(photoPreview, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(180)));

        LinearLayout actions = buttonRow();
        Button live = makeButton("Take Live Photo", true);
        Button upload = makeButton("Upload Photo", false);
        actions.addView(live, weightedButtonParams(true));
        actions.addView(upload, weightedButtonParams(false));
        block.addView(actions);

        live.setOnClickListener(v -> openCamera(PHOTO_REQUEST));
        upload.setOnClickListener(v -> openImagePicker(PHOTO_PICK_REQUEST));
        return block;
    }

    private View buildSignatureBlock() {
        LinearLayout block = new LinearLayout(this);
        block.setOrientation(LinearLayout.VERTICAL);
        block.setPadding(0, dp(10), 0, 0);

        TextView label = label("Applicant Signature Photo *");
        block.addView(label);

        signaturePhotoPreview = new ImageView(this);
        signaturePhotoPreview.setBackground(rounded(Color.rgb(242, 245, 242), dp(12), 1, BORDER));
        signaturePhotoPreview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        signaturePhotoPreview.setAdjustViewBounds(true);
        block.addView(signaturePhotoPreview, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(135)));

        LinearLayout actions = buttonRow();
        Button live = makeButton("Take Signature Photo", true);
        Button upload = makeButton("Upload Signature", false);
        actions.addView(live, weightedButtonParams(true));
        actions.addView(upload, weightedButtonParams(false));
        block.addView(actions);

        live.setOnClickListener(v -> openCamera(SIGNATURE_PHOTO_REQUEST));
        upload.setOnClickListener(v -> openImagePicker(SIGNATURE_PICK_REQUEST));
        return block;
    }

    private void submitRegistration() {
        updateAge();

        if (blank(eapDateField) ||
                "Select".equals(selectedSpinner(projectLocationSpinner)) ||
                blank(nameField) ||
                selectedRadio(genderGroup).isEmpty() ||
                blank(dobField) ||
                blank(ageField) ||
                blank(guardianNameField) ||
                blank(villageField) ||
                blank(panchayatField) ||
                blank(pinCodeField) ||
                blank(mobileField) ||
                blank(educationField) ||
                blank(occupationField) ||
                "Select".equals(selectedSpinner(categorySpinner)) ||
                selectedRadio(intentionGroup).isEmpty() ||
                selectedRadio(sectorGroup).isEmpty() ||
                selectedRadio(msdpGroup).isEmpty() ||
                photoBase64.isEmpty() ||
                signaturePhotoBase64.isEmpty() ||
                !declarationCheck.isChecked()) {
            Toast.makeText(this,
                    "Please complete all mandatory fields, photo, signature and declaration.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String pin = value(pinCodeField);
        String mobile = value(mobileField);
        String altMobile = value(alternateMobileField);
        String aadhaar = value(aadhaarField);

        if (!pin.matches("^[1-9][0-9]{5}$")) {
            Toast.makeText(this, "Enter a valid 6 digit PIN code.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!mobile.matches("^[6-9][0-9]{9}$")) {
            Toast.makeText(this, "Enter a valid 10 digit mobile number.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!altMobile.isEmpty() && !altMobile.matches("^[6-9][0-9]{9}$")) {
            Toast.makeText(this, "Enter a valid alternate mobile number.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!aadhaar.isEmpty() && !aadhaar.matches("^[0-9]{12}$")) {
            Toast.makeText(this, "Aadhaar number must contain exactly 12 digits.", Toast.LENGTH_LONG).show();
            return;
        }

        submitButton.setEnabled(false);
        submitButton.setText("Saving...");

        try {
            String projectLocation = selectedSpinner(projectLocationSpinner);
            String locationCode = projectLocationCode(projectLocation);

            SharedPreferences serialPrefs = getSharedPreferences("serial_counters_2026_v2", MODE_PRIVATE);
            int totalNumber = serialPrefs.getInt("total_2026", 0) + 1;
            int locationNumber = serialPrefs.getInt("location_2026_" + locationCode, 0) + 1;

            String totalSerial = "EAP/2026/" + formatSerialNumber(totalNumber);
            String locationSerial = "EAP/2026/" + locationCode + "/" + formatSerialNumber(locationNumber);

            JSONObject payload = new JSONObject();
            payload.put("totalSerialNumber", totalSerial);
            payload.put("individualSerialNumber", locationSerial);
            payload.put("totalApplicationCount", totalNumber);
            payload.put("locationApplicationCount", locationNumber);
            payload.put("locationCode", locationCode);

            payload.put("eapDate", value(eapDateField));
            payload.put("name", value(nameField));
            payload.put("gender", selectedRadio(genderGroup));
            payload.put("dob", value(dobField));
            payload.put("ageOnEapDate", value(ageField));
            payload.put("guardianName", value(guardianNameField));

            payload.put("village", value(villageField));
            payload.put("panchayat", value(panchayatField));
            payload.put("block", "");
            payload.put("city", projectPlace(projectLocation));
            payload.put("pinCode", pin);
            payload.put("state", "Karnataka");
            payload.put("projectLocation", projectLocation);
            payload.put("place", projectPlace(projectLocation));
            payload.put("fullAddress", buildFullAddress(
                    value(villageField),
                    value(panchayatField),
                    projectLocation,
                    "Karnataka",
                    pin));

            payload.put("mobile", mobile);
            payload.put("alternateMobile", altMobile);
            payload.put("email", value(emailField));
            payload.put("idType", "Aadhaar");
            payload.put("idNumber", aadhaar);
            payload.put("education", value(educationField));
            payload.put("occupation", value(occupationField));
            payload.put("individualIncome", value(incomeField));
            payload.put("category", selectedSpinner(categorySpinner));
            payload.put("intention", selectedRadio(intentionGroup));
            payload.put("sector", selectedRadio(sectorGroup));
            payload.put("sectors", selectedRadio(sectorGroup));
            payload.put("msdpInterest", selectedRadio(msdpGroup));
            payload.put("photoBase64", photoBase64);
            payload.put("signatureType", "Photo");
            payload.put("signatureBase64", signaturePhotoBase64);
            payload.put("declarationAccepted", true);

            String requestId = UUID.randomUUID().toString();
            payload.put("clientRequestId", requestId);

            PdfGenerator.Result pdf = PdfGenerator.createAndSave(this, payload, requestId);
            payload.put("pdfFileName", pdf.fileName);
            payload.put("pdfBase64", pdf.base64);

            RegistrationStore store = new RegistrationStore(this);
            store.savePending(requestId, payload.toString());

            serialPrefs.edit()
                    .putInt("total_2026", totalNumber)
                    .putInt("location_2026_" + locationCode, locationNumber)
                    .apply();

            scheduleSync();
            updateSerialPreview();

            saveStatusView.setText("Saved locally. PDF: " + pdf.savedLocation +
                    " | Pending sync: " + store.pendingCount());

            showSuccessDialog(
                    value(nameField),
                    totalSerial,
                    locationSerial
            );
        } catch (Exception ex) {
            submitButton.setEnabled(true);
            submitButton.setText("Submit Application");
            saveStatusView.setText("Unable to save: " + ex.getMessage());
            Toast.makeText(this, "Unable to save registration.", Toast.LENGTH_LONG).show();
        }
    }

    private void showSuccessDialog(String applicantName, String totalSerial, String locationSerial) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER_HORIZONTAL);
        box.setPadding(dp(24), dp(22), dp(24), dp(18));
        box.setBackgroundColor(Color.WHITE);

        TextView check = new TextView(this);
        check.setText("✓");
        check.setGravity(Gravity.CENTER);
        check.setTextSize(38);
        check.setTextColor(Color.WHITE);
        check.setBackground(rounded(GREEN, dp(45), 0, 0));
        box.addView(check, new LinearLayout.LayoutParams(dp(82), dp(82)));

        TextView name = new TextView(this);
        name.setText(applicantName);
        name.setTextColor(GREEN_DARK);
        name.setTextSize(23);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setGravity(Gravity.CENTER);
        name.setPadding(0, dp(14), 0, dp(4));
        box.addView(name);

        TextView message = new TextView(this);
        message.setText("application is submitted successfully.");
        message.setTextColor(TEXT);
        message.setTextSize(16);
        message.setGravity(Gravity.CENTER);
        box.addView(message);

        TextView serials = new TextView(this);
        serials.setText("Application Serial No.\n" + totalSerial +
                "\n\nLocation Serial No.\n" + locationSerial);
        serials.setTextColor(TEXT);
        serials.setTextSize(15);
        serials.setTypeface(Typeface.DEFAULT_BOLD);
        serials.setPadding(dp(16), dp(14), dp(16), dp(14));
        serials.setBackground(rounded(GREEN_LIGHT, dp(14), 0, 0));
        LinearLayout.LayoutParams serialLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        serialLp.setMargins(0, dp(18), 0, dp(14));
        box.addView(serials, serialLp);

        Button next = makeButton("Next Applicant", true);
        box.addView(next, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(box)
                .setCancelable(false)
                .create();

        next.setOnClickListener(v -> {
            dialog.dismiss();
            clearForNextApplicant();
            submitButton.setEnabled(true);
            submitButton.setText("Submit Application");
            updateSerialPreview();
            homeScroll.post(() -> homeScroll.fullScroll(View.FOCUS_UP));
        });

        dialog.show();
    }

    private void clearForNextApplicant() {
        nameField.setText("");
        genderGroup.clearCheck();
        dobValue = null;
        dobField.setText("");
        ageField.setText("");
        guardianNameField.setText("");

        villageField.setText("");
        panchayatField.setText("");
        pinCodeField.setText("");
        cityField.setText(projectPlace(selectedSpinner(projectLocationSpinner)));

        mobileField.setText("");
        alternateMobileField.setText("");
        emailField.setText("");
        aadhaarField.setText("");
        educationField.setText("");
        occupationField.setText("");
        incomeField.setText("");

        categorySpinner.setSelection(0);
        intentionGroup.clearCheck();
        sectorGroup.clearCheck();
        msdpGroup.clearCheck();
        declarationCheck.setChecked(false);

        photoBase64 = "";
        photoPreview.setImageDrawable(null);
        signaturePhotoBase64 = "";
        signaturePhotoPreview.setImageDrawable(null);
        saveStatusView.setText("");

        Toast.makeText(this, "Ready for next applicant.", Toast.LENGTH_SHORT).show();
    }

    private void showApplications() {
        setActiveNav(navApplications);

        ScrollView scroll = new ScrollView(this);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(14), dp(12), dp(14), dp(20));
        scroll.addView(page);

        TextView title = pageTitle("Applications");
        page.addView(title);

        RegistrationStore store = new RegistrationStore(this);
        List<RegistrationStore.Record> records = store.getRecords(250);

        TextView counts = new TextView(this);
        counts.setText("All " + store.totalCount() +
                "   •   Today " + store.todayCount() +
                "   •   Pending " + store.pendingCount());
        counts.setTextColor(MUTED);
        counts.setTextSize(13);
        counts.setPadding(dp(2), 0, 0, dp(10));
        page.addView(counts);

        EditText search = createInput("");
        search.setHint("Search by name, mobile or serial no.");
        page.addView(search, cardParams(0, dp(8)));

        LinearLayout filters = buttonRow();
        Button all = makeButton("All", true);
        Button today = makeButton("Today", false);
        Button pending = makeButton("Pending", false);
        filters.addView(all, weightedButtonParams(true));
        filters.addView(today, weightedButtonParams(false));
        filters.addView(pending, weightedButtonParams(false));
        page.addView(filters);

        LinearLayout listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        page.addView(listContainer);

        final String[] activeFilter = {"ALL"};
        Runnable render = () -> renderApplicationList(
                listContainer,
                records,
                value(search).toLowerCase(Locale.ENGLISH),
                activeFilter[0]);

        all.setOnClickListener(v -> {
            activeFilter[0] = "ALL";
            render.run();
        });
        today.setOnClickListener(v -> {
            activeFilter[0] = "TODAY";
            render.run();
        });
        pending.setOnClickListener(v -> {
            activeFilter[0] = "PENDING";
            render.run();
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                render.run();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        render.run();
        attachContent(scroll);
    }

    private void renderApplicationList(
            LinearLayout target,
            List<RegistrationStore.Record> records,
            String query,
            String filter) {

        target.removeAllViews();
        long todayStart = startOfToday();

        int shown = 0;
        for (RegistrationStore.Record record : records) {
            try {
                JSONObject data = new JSONObject(record.payload);
                String name = data.optString("name", "Applicant");
                String mobile = data.optString("mobile", "");
                String totalSerial = data.optString("totalSerialNumber", "");
                String locationSerial = data.optString("individualSerialNumber", "");

                boolean isToday = record.createdAt >= todayStart;
                boolean isPending = !"SYNCED".equals(record.status);

                if ("TODAY".equals(filter) && !isToday) continue;
                if ("PENDING".equals(filter) && !isPending) continue;

                String haystack = (name + " " + mobile + " " + totalSerial + " " + locationSerial)
                        .toLowerCase(Locale.ENGLISH);
                if (!query.isEmpty() && !haystack.contains(query)) continue;

                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setPadding(dp(14), dp(12), dp(14), dp(12));
                card.setBackground(rounded(Color.WHITE, dp(14), 1, BORDER));
                card.setElevation(dp(1));

                TextView nameView = new TextView(this);
                nameView.setText(name);
                nameView.setTextColor(TEXT);
                nameView.setTextSize(16);
                nameView.setTypeface(Typeface.DEFAULT_BOLD);
                card.addView(nameView);

                TextView serialView = new TextView(this);
                serialView.setText(totalSerial + (locationSerial.isEmpty() ? "" : "   •   " + locationSerial));
                serialView.setTextColor(MUTED);
                serialView.setTextSize(12);
                serialView.setPadding(0, dp(3), 0, 0);
                card.addView(serialView);

                TextView meta = new TextView(this);
                meta.setText(historyDateFormat.format(record.createdAt) +
                        "   •   " + ("SYNCED".equals(record.status) ? "Submitted" : "Pending Sync"));
                meta.setTextColor("SYNCED".equals(record.status) ? GREEN_DARK : Color.rgb(190, 118, 0));
                meta.setTextSize(12);
                meta.setPadding(0, dp(4), 0, 0);
                card.addView(meta);

                if (record.lastError != null && !record.lastError.trim().isEmpty() && isPending) {
                    TextView error = new TextView(this);
                    error.setText(record.lastError);
                    error.setTextColor(Color.rgb(170, 60, 60));
                    error.setTextSize(11);
                    error.setPadding(0, dp(4), 0, 0);
                    card.addView(error);
                }

                target.addView(card, cardParams(0, dp(6)));
                shown++;
            } catch (Exception ignored) {}
        }

        if (shown == 0) {
            TextView empty = new TextView(this);
            empty.setText("No applications found.");
            empty.setTextColor(MUTED);
            empty.setTextSize(15);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(40), 0, dp(40));
            target.addView(empty);
        }
    }

    private void showMore() {
        setActiveNav(navMore);

        ScrollView scroll = new ScrollView(this);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(14), dp(12), dp(14), dp(20));
        scroll.addView(page);

        page.addView(pageTitle("More"));

        LinearLayout syncCard = createSectionCard(page, "Google Sync Settings");

        SharedPreferences prefs = getSharedPreferences("sync_settings", MODE_PRIVATE);
        EditText url = createInput(prefs.getString("api_url", ""));
        url.setHint("Apps Script Web App URL");

        EditText token = createInput(prefs.getString("api_token", ""));
        token.setHint("API Token");
        token.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        syncCard.addView(fieldBlock("Apps Script Web App URL", url));
        syncCard.addView(fieldBlock("API Token", token));

        TextView pending = new TextView(this);
        pending.setTextColor(MUTED);
        pending.setTextSize(13);
        pending.setPadding(dp(2), dp(6), 0, dp(8));
        syncCard.addView(pending);
        refreshPending(pending);

        LinearLayout actions = buttonRow();
        Button save = makeButton("Save Settings", true);
        Button sync = makeButton("Sync Now", false);
        actions.addView(save, weightedButtonParams(true));
        actions.addView(sync, weightedButtonParams(false));
        syncCard.addView(actions);

        save.setOnClickListener(v -> {
            String u = value(url);
            String t = value(token);

            if (!u.isEmpty() && !u.startsWith("https://")) {
                Toast.makeText(this, "Enter a valid HTTPS Apps Script URL.", Toast.LENGTH_LONG).show();
                return;
            }

            prefs.edit()
                    .putString("api_url", u)
                    .putString("api_token", t)
                    .apply();

            scheduleSync();
            refreshPending(pending);
            Toast.makeText(this, "Sync settings saved.", Toast.LENGTH_SHORT).show();
        });

        sync.setOnClickListener(v -> {
            scheduleSync();
            refreshPending(pending);
            Toast.makeText(this, "Sync queued. It will run when internet is available.", Toast.LENGTH_SHORT).show();
        });

        LinearLayout appCard = createSectionCard(page, "App Information");

        TextView info = new TextView(this);
        info.setText("HAL EAP Registration\nVersion: " + appVersion() +
                "\nPDF storage: Downloads / EAP Registrations\nOffline registration: Enabled");
        info.setTextColor(TEXT);
        info.setTextSize(14);
        info.setLineSpacing(0, 1.25f);
        appCard.addView(info);

        attachContent(scroll);
    }

    private String appVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception ex) {
            return "";
        }
    }

    private void attachContent(View view) {
        contentFrame.removeAllViews();
        contentFrame.addView(view, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
    }

    private LinearLayout createSectionCard(LinearLayout parent, String titleText) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(rounded(Color.WHITE, dp(16), 1, BORDER));
        card.setElevation(dp(2));

        TextView header = new TextView(this);
        header.setText(titleText);
        header.setTextColor(GREEN_DARK);
        header.setTextSize(17);
        header.setTypeface(Typeface.DEFAULT_BOLD);
        header.setPadding(dp(14), dp(10), dp(14), dp(10));
        header.setBackground(rounded(GREEN_LIGHT, dp(15), 0, 0));
        card.addView(header);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(12), dp(8), dp(12), dp(12));
        card.addView(content);

        parent.addView(card, cardParams(0, dp(7)));
        return content;
    }

    private LinearLayout fieldBlock(String labelText, View field) {
        LinearLayout block = new LinearLayout(this);
        block.setOrientation(LinearLayout.VERTICAL);
        block.setPadding(0, dp(4), 0, dp(4));

        block.addView(label(labelText));
        block.addView(field, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        return block;
    }

    private TextView label(String text) {
        TextView l = new TextView(this);
        l.setText(text);
        l.setTextColor(TEXT);
        l.setTextSize(13);
        l.setTypeface(Typeface.DEFAULT_BOLD);
        l.setPadding(dp(2), 0, 0, dp(5));
        return l;
    }

    private TextView pageTitle(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(TEXT);
        t.setTextSize(22);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setPadding(dp(2), dp(2), 0, dp(12));
        return t;
    }

    private EditText createInput(String initial) {
        EditText e = new EditText(this);
        e.setSingleLine(true);
        e.setText(initial == null ? "" : initial);
        e.setTextColor(TEXT);
        e.setHintTextColor(Color.rgb(145, 151, 147));
        e.setTextSize(15);
        e.setPadding(dp(12), dp(10), dp(12), dp(10));
        e.setMinHeight(dp(48));
        e.setBackground(rounded(Color.WHITE, dp(10), 1, BORDER));
        return e;
    }

    private Spinner createSpinner(String[] items) {
        Spinner s = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                items) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(TEXT);
                    ((TextView) view).setTextSize(15);
                    view.setPadding(dp(12), dp(10), dp(12), dp(10));
                }
                return view;
            }
        };
        s.setAdapter(adapter);
        s.setMinimumHeight(dp(48));
        s.setPadding(dp(4), 0, dp(4), 0);
        s.setBackground(rounded(Color.WHITE, dp(10), 1, BORDER));
        return s;
    }

    private RadioGroup createRadioGroup(String[] items, boolean horizontal) {
        RadioGroup group = new RadioGroup(this);
        group.setOrientation(horizontal ? RadioGroup.HORIZONTAL : RadioGroup.VERTICAL);
        group.setPadding(dp(2), dp(2), dp(2), dp(2));

        for (String item : items) {
            RadioButton rb = new RadioButton(this);
            rb.setText(item);
            rb.setTextColor(TEXT);
            rb.setTextSize(14);
            rb.setButtonTintList(android.content.res.ColorStateList.valueOf(GREEN));

            if (horizontal) {
                group.addView(rb, new RadioGroup.LayoutParams(
                        0, RadioGroup.LayoutParams.WRAP_CONTENT, 1f));
            } else {
                group.addView(rb);
            }
        }
        return group;
    }

    private void addPair(LinearLayout parent, View first, View second) {
        boolean wide = getResources().getConfiguration().screenWidthDp >= 420;
        if (!wide) {
            parent.addView(first);
            parent.addView(second);
            return;
        }

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams left = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        left.setMargins(0, 0, dp(5), 0);

        LinearLayout.LayoutParams right = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        right.setMargins(dp(5), 0, 0, 0);

        row.addView(first, left);
        row.addView(second, right);
        parent.addView(row);
    }

    private LinearLayout buttonRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(8), 0, 0);
        return row;
    }

    private Button makeButton(String text, boolean primary) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(14);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setTextColor(primary ? Color.WHITE : GREEN_DARK);
        button.setPadding(dp(10), dp(9), dp(10), dp(9));
        button.setMinHeight(dp(46));
        button.setBackground(rounded(
                primary ? GREEN : Color.WHITE,
                dp(12),
                1,
                primary ? GREEN : GREEN));
        return button;
    }

    private LinearLayout.LayoutParams weightedButtonParams(boolean left) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        if (left) lp.setMargins(0, 0, dp(5), 0);
        else lp.setMargins(dp(5), 0, 0, 0);
        return lp;
    }

    private LinearLayout.LayoutParams cardParams(int top, int bottom) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, top, 0, bottom);
        return lp;
    }

    private View divider() {
        View v = new View(this);
        v.setBackgroundColor(BORDER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
        lp.setMargins(0, dp(12), 0, dp(4));
        v.setLayoutParams(lp);
        return v;
    }

    private GradientDrawable rounded(int color, float radius, int strokeWidth, int strokeColor) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        if (strokeWidth > 0) d.setStroke(dp(strokeWidth), strokeColor);
        return d;
    }

    private GradientDrawable gradient(int start, int end, float radius) {
        GradientDrawable d = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{start, end});
        d.setCornerRadius(radius);
        return d;
    }

    private void openCamera(int requestCode) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, requestCode);
        } else {
            Toast.makeText(this, "Camera is not available.", Toast.LENGTH_LONG).show();
        }
    }

    private void openImagePicker(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;

        if ((requestCode == PHOTO_PICK_REQUEST || requestCode == SIGNATURE_PICK_REQUEST)
                && data != null && data.getData() != null) {
            try {
                Bitmap bitmap = loadBitmapFromUri(data.getData());
                if (bitmap == null) {
                    Toast.makeText(this, "Unable to open selected photo.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (requestCode == PHOTO_PICK_REQUEST) {
                    setCandidatePhoto(bitmap);
                } else {
                    setSignaturePhoto(bitmap);
                }
            } catch (Exception ex) {
                Toast.makeText(this, "Unable to load selected photo.", Toast.LENGTH_LONG).show();
            }
            return;
        }

        if (data == null) return;

        Bundle extras = data.getExtras();
        Object obj = extras == null ? null : extras.get("data");
        if (!(obj instanceof Bitmap)) return;

        Bitmap bitmap = (Bitmap) obj;
        if (requestCode == PHOTO_REQUEST) {
            setCandidatePhoto(bitmap);
        } else if (requestCode == SIGNATURE_PHOTO_REQUEST) {
            setSignaturePhoto(bitmap);
        }
    }

    private void setCandidatePhoto(Bitmap bitmap) {
        photoPreview.setImageBitmap(bitmap);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 88, out);
        photoBase64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
    }

    private void setSignaturePhoto(Bitmap bitmap) {
        signaturePhotoPreview.setImageBitmap(bitmap);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 88, out);
        signaturePhotoBase64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
    }

    private Bitmap loadBitmapFromUri(Uri uri) throws Exception {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;

        try (InputStream input = getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(input, null, bounds);
        }

        int sample = 1;
        int maxDimension = Math.max(bounds.outWidth, bounds.outHeight);
        while (maxDimension / sample > 1600) {
            sample *= 2;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = Math.max(1, sample);

        try (InputStream input = getContentResolver().openInputStream(uri)) {
            return BitmapFactory.decodeStream(input, null, options);
        }
    }

    private void showDatePicker(EditText target) {
        Calendar initial = Calendar.getInstance();

        if (target == eapDateField && eapDateValue != null) {
            initial.setTimeInMillis(eapDateValue.getTimeInMillis());
        } else if (target == dobField && dobValue != null) {
            initial.setTimeInMillis(dobValue.getTimeInMillis());
        } else if (target == dobField && eapDateValue != null) {
            initial.setTimeInMillis(eapDateValue.getTimeInMillis());
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.clear();
                    selected.set(year, month, day, 0, 0, 0);
                    selected.set(Calendar.MILLISECOND, 0);

                    if (target == eapDateField) {
                        eapDateValue = (Calendar) selected.clone();
                    } else if (target == dobField) {
                        dobValue = (Calendar) selected.clone();
                    }

                    target.setText(sdf.format(selected.getTime()));
                    updateAge();
                },
                initial.get(Calendar.YEAR),
                initial.get(Calendar.MONTH),
                initial.get(Calendar.DAY_OF_MONTH));

        if (target == dobField && eapDateValue != null) {
            dialog.getDatePicker().setMaxDate(eapDateValue.getTimeInMillis());
        }

        dialog.show();
    }

    private void updateAge() {
        if (dobValue == null || eapDateValue == null || ageField == null) {
            if (ageField != null) ageField.setText("");
            return;
        }

        Calendar dob = (Calendar) dobValue.clone();
        Calendar event = (Calendar) eapDateValue.clone();

        if (event.before(dob)) {
            ageField.setText("");
            Toast.makeText(this, "Date of Birth cannot be after EAP Date.", Toast.LENGTH_LONG).show();
            return;
        }

        int age = event.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        int eventMonth = event.get(Calendar.MONTH);
        int dobMonth = dob.get(Calendar.MONTH);

        if (eventMonth < dobMonth ||
                (eventMonth == dobMonth &&
                        event.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH))) {
            age--;
        }

        ageField.setText(String.valueOf(Math.max(age, 0)));
    }

    private void updateSerialPreview() {
        if (totalSerialView == null || locationSerialView == null) return;

        SharedPreferences serialPrefs = getSharedPreferences("serial_counters_2026_v2", MODE_PRIVATE);

        int totalSubmitted = serialPrefs.getInt("total_2026", 0);
        int nextTotal = totalSubmitted + 1;

        totalSerialView.setText(
                "Total Applications Submitted\n" +
                totalSubmitted +
                "\nNext Serial  EAP/2026/" + formatSerialNumber(nextTotal));

        if (projectLocationSpinner == null) {
            locationSerialView.setText("Location Applications Submitted\n0\nSelect location");
            return;
        }

        String selected = selectedSpinner(projectLocationSpinner);
        if (selected.isEmpty() || "Select".equals(selected)) {
            locationSerialView.setText(
                    "Location Applications Submitted\n0\nSelect HAL Project Location");
            return;
        }

        String code = projectLocationCode(selected);
        int submitted = serialPrefs.getInt("location_2026_" + code, 0);
        int next = submitted + 1;

        locationSerialView.setText(
                "Location Applications Submitted\n" +
                submitted +
                "\nNext Serial  EAP/2026/" + code + "/" + formatSerialNumber(next));
    }

    private void refreshPending(TextView view) {
        RegistrationStore store = new RegistrationStore(this);
        view.setText("Pending sync: " + store.pendingCount() +
                "   •   Total local records: " + store.totalCount());
    }

    private void scheduleSync() {
        try {
            JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo info = new JobInfo.Builder(
                    SYNC_JOB_ID,
                    new ComponentName(this, SyncJobService.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setBackoffCriteria(30000, JobInfo.BACKOFF_POLICY_LINEAR)
                    .build();
            scheduler.schedule(info);
        } catch (Exception ignored) {}
    }

    private String selectedRadio(RadioGroup group) {
        if (group == null) return "";
        int id = group.getCheckedRadioButtonId();
        if (id == -1) return "";
        RadioButton rb = findViewById(id);
        return rb == null ? "" : rb.getText().toString();
    }

    private String selectedSpinner(Spinner spinner) {
        if (spinner == null) return "";
        Object item = spinner.getSelectedItem();
        return item == null ? "" : item.toString();
    }

    private boolean blank(EditText field) {
        return value(field).isEmpty();
    }

    private String value(EditText field) {
        return field == null || field.getText() == null
                ? ""
                : field.getText().toString().trim();
    }

    private String buildFullAddress(
            String village,
            String panchayat,
            String projectLocation,
            String state,
            String pinCode) {

        StringBuilder sb = new StringBuilder();
        appendAddressPart(sb, village);
        appendAddressPart(sb, panchayat);
        appendAddressPart(sb, projectAddressPart(projectLocation));
        appendAddressPart(sb, state);

        if (pinCode != null && !pinCode.trim().isEmpty()) {
            appendAddressPart(sb, "PIN " + pinCode.trim());
        }
        return sb.toString();
    }

    private void appendAddressPart(StringBuilder sb, String value) {
        if (value == null || value.trim().isEmpty()) return;
        if (sb.length() > 0) sb.append(", ");
        sb.append(value.trim());
    }

    private String projectAddressPart(String projectLocation) {
        if (projectLocation == null ||
                projectLocation.isEmpty() ||
                "Select".equals(projectLocation)) {
            return "";
        }

        int sep = projectLocation.indexOf("|");
        if (sep < 0) return projectLocation.trim();

        String district = projectLocation.substring(0, sep).trim();
        String place = projectLocation.substring(sep + 1).trim();

        if (district.equalsIgnoreCase(place)) return place;
        return place + ", " + district;
    }

    private String projectLocationCode(String projectLocation) {
        if (projectLocation == null) return "NA";

        switch (projectLocation) {
            case "Kolar | Mulbagal": return "K-M";
            case "Kolar | Srinivaspur": return "K-S";
            case "Bengaluru Rural | Devanahalli": return "BR-D";
            case "Bengaluru Rural | Hoskote": return "BR-H";
            case "Tumkur | Tumkur": return "T-T";
            case "Tumkur | Gubbi": return "T-G";
            case "Tumkur | Sira": return "T-S";
            case "Bengaluru South | Ramanagara": return "BS-R";
            case "Bengaluru South | Channapatna": return "BS-C";
            default: return "NA";
        }
    }

    private String projectPlace(String projectLocation) {
        if (projectLocation == null || "Select".equals(projectLocation)) return "";
        int sep = projectLocation.indexOf("|");
        return sep >= 0
                ? projectLocation.substring(sep + 1).trim()
                : projectLocation.trim();
    }

    private String formatSerialNumber(int number) {
        return String.format(Locale.ENGLISH, "%02d", number);
    }

    private long startOfToday() {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        return start.getTimeInMillis();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
