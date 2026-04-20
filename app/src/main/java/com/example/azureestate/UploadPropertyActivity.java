package com.example.azureestate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UploadPropertyActivity extends AppCompatActivity {

    // Step state
    private LinearLayout layoutUploadStep1, layoutUploadStep2, layoutUploadStep3, layoutUploadStep4;
    private View stepBar1, stepBar2, stepBar3, stepBar4;
    private TextView tvStepLabel;
    private int currentStep = 1;

    // Step 1 — Photos
    private final List<Uri> selectedPhotoUris = new ArrayList<>();
    private androidx.gridlayout.widget.GridLayout photoGrid;
    private Button btnStep1Next, btnAddPhoto;

    // Step 2 — Basics
    private TextInputEditText etTitle, etPrice, etAddress;
    private TextInputLayout tilTitle, tilPrice, tilAddress;
    private TextView uploadCatHouse, uploadCatApartment, uploadCatVilla, uploadCatCondo;
    private String selectedCategory = "";
    private Button btnStep2Back, btnStep2Next;

    // Step 3 — Details
    private TextInputEditText etBeds, etBaths, etSqft, etGarages, etDescription, etContactPhone;
    private TextInputLayout tilBeds, tilBaths, tilSqft, tilContactPhone;
    private Button btnStep3Back, btnStep3Next;

    // Step 4 — Review & Publish
    private ImageView ivPreviewCover;
    private TextView tvPreviewTitle, tvPreviewAddress, tvPreviewPrice;
    private CheckBox cbListingTerms;
    private Button btnStep4Back, btnPublish;

    // Supabase & Firebase
    private SupabaseManager supabase;
    private FirebaseAuth auth;

    // Photo picker
    private final ActivityResultLauncher<String[]> photoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(), uris -> {
                if (uris == null || uris.isEmpty()) return;
                selectedPhotoUris.clear();
                int count = Math.min(uris.size(), 10);
                for (int i = 0; i < count; i++) {
                    Uri uri = uris.get(i);
                    selectedPhotoUris.add(uri);
                    try {
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (SecurityException ignored) {}
                }
                refreshPhotoGrid();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_property);

        auth = FirebaseAuth.getInstance();
        supabase = SupabaseManager.getInstance(this);

        bindViews();
        setupCategoryChips();
        setupButtons();
    }

    private void bindViews() {
        // Steps
        layoutUploadStep1 = findViewById(R.id.layoutUploadStep1);
        layoutUploadStep2 = findViewById(R.id.layoutUploadStep2);
        layoutUploadStep3 = findViewById(R.id.layoutUploadStep3);
        layoutUploadStep4 = findViewById(R.id.layoutUploadStep4);
        stepBar1 = findViewById(R.id.stepBar1);
        stepBar2 = findViewById(R.id.stepBar2);
        stepBar3 = findViewById(R.id.stepBar3);
        stepBar4 = findViewById(R.id.stepBar4);
        tvStepLabel = findViewById(R.id.tvStepLabel);
        photoGrid = findViewById(R.id.photoGrid);
        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        btnStep1Next = findViewById(R.id.btnStep1Next);

        // Step 2
        etTitle = findViewById(R.id.etTitle);
        tilTitle = findViewById(R.id.tilTitle);
        etPrice = findViewById(R.id.etPrice);
        tilPrice = findViewById(R.id.tilPrice);
        etAddress = findViewById(R.id.etAddress);
        tilAddress = findViewById(R.id.tilAddress);
        uploadCatHouse = findViewById(R.id.uploadCatHouse);
        uploadCatApartment = findViewById(R.id.uploadCatApartment);
        uploadCatVilla = findViewById(R.id.uploadCatVilla);
        uploadCatCondo = findViewById(R.id.uploadCatCondo);
        btnStep2Back = findViewById(R.id.btnStep2Back);
        btnStep2Next = findViewById(R.id.btnStep2Next);

        // Step 3
        etBeds = findViewById(R.id.etBeds);
        tilBeds = findViewById(R.id.tilBeds);
        etBaths = findViewById(R.id.etBaths);
        tilBaths = findViewById(R.id.tilBaths);
        etSqft = findViewById(R.id.etSqft);
        tilSqft = findViewById(R.id.tilSqft);
        etGarages = findViewById(R.id.etGarages);
        etDescription = findViewById(R.id.etDescription);
        etContactPhone = findViewById(R.id.etContactPhone);
        tilContactPhone = findViewById(R.id.tilContactPhone);
        btnStep3Back = findViewById(R.id.btnStep3Back);
        btnStep3Next = findViewById(R.id.btnStep3Next);

        // Step 4
        ivPreviewCover = findViewById(R.id.ivPreviewCover);
        tvPreviewTitle = findViewById(R.id.tvPreviewTitle);
        tvPreviewAddress = findViewById(R.id.tvPreviewAddress);
        tvPreviewPrice = findViewById(R.id.tvPreviewPrice);
        cbListingTerms = findViewById(R.id.cbListingTerms);
        btnStep4Back = findViewById(R.id.btnStep4Back);
        btnPublish = findViewById(R.id.btnPublish);

        // Back arrow
        View ivBack = findViewById(R.id.ivUploadBack);
        if (ivBack != null) ivBack.setOnClickListener(v -> onBackPressed());

        // Add photo button
        if (btnAddPhoto != null) {
            btnAddPhoto.setOnClickListener(v -> photoPickerLauncher.launch(new String[]{"image/*"}));
        }
    }

    private void setupCategoryChips() {
        View.OnClickListener catClick = v -> setActiveCategory(((TextView) v).getText().toString());
        if (uploadCatHouse != null) uploadCatHouse.setOnClickListener(catClick);
        if (uploadCatApartment != null) uploadCatApartment.setOnClickListener(catClick);
        if (uploadCatVilla != null) uploadCatVilla.setOnClickListener(catClick);
        if (uploadCatCondo != null) uploadCatCondo.setOnClickListener(catClick);
    }

    private void setActiveCategory(String cat) {
        selectedCategory = cat;
        TextView[] chips = {uploadCatHouse, uploadCatApartment, uploadCatVilla, uploadCatCondo};
        for (TextView c : chips) {
            if (c == null) continue;
            boolean active = c.getText().toString().equals(cat);
            c.setBackgroundResource(active ? R.drawable.chip_filter_active : R.drawable.chip_inactive);
            c.setTextColor(active ? 0xFFFFFFFF : 0xFF7A8899);
            if (active) {
                c.animate().scaleX(1.06f).scaleY(1.06f).setDuration(90)
                        .withEndAction(() -> c.animate().scaleX(1f).scaleY(1f).setDuration(90).start()).start();
            }
        }
    }

    private void setupButtons() {
        if (btnStep1Next != null) {
            btnStep1Next.setOnClickListener(v -> {
                if (selectedPhotoUris.isEmpty()) {
                    Toast.makeText(this, "Please add at least one photo", Toast.LENGTH_SHORT).show();
                    return;
                }
                goToStep(2);
            });
        }

        if (btnStep2Back != null) btnStep2Back.setOnClickListener(v -> goToStep(1));
        if (btnStep2Next != null) {
            btnStep2Next.setOnClickListener(v -> {
                if (validateStep2()) goToStep(3);
            });
        }

        if (btnStep3Back != null) btnStep3Back.setOnClickListener(v -> goToStep(2));
        if (btnStep3Next != null) {
            btnStep3Next.setOnClickListener(v -> {
                if (validateStep3()) {
                    populatePreview();
                    goToStep(4);
                }
            });
        }

        if (btnStep4Back != null) btnStep4Back.setOnClickListener(v -> goToStep(3));
        if (btnPublish != null) btnPublish.setOnClickListener(v -> publishListing());
    }

    private void refreshPhotoGrid() {
        if (photoGrid == null) return;
        int childCount = photoGrid.getChildCount();
        if (childCount > 1) photoGrid.removeViews(1, childCount - 1);

        for (int i = 0; i < selectedPhotoUris.size(); i++) {
            final Uri uri = selectedPhotoUris.get(i);
            final int idx = i;

            FrameLayout frame = new FrameLayout(this);
            androidx.gridlayout.widget.GridLayout.LayoutParams lp = new androidx.gridlayout.widget.GridLayout.LayoutParams();
            lp.width = 0;
            lp.height = dpToPx(100);
            lp.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
            lp.columnSpec = androidx.gridlayout.widget.GridLayout.spec(androidx.gridlayout.widget.GridLayout.UNDEFINED, 1f);
            frame.setLayoutParams(lp);
            frame.setBackground(getDrawable(R.drawable.photo_slot_filled));

            ImageView iv = new ImageView(this);
            iv.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(this).load(uri).centerCrop().into(iv);
            frame.addView(iv);

            if (i == 0) {
                TextView coverBadge = new TextView(this);
                FrameLayout.LayoutParams badgeLp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                badgeLp.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.START;
                badgeLp.setMargins(dpToPx(6), 0, 0, dpToPx(6));
                coverBadge.setLayoutParams(badgeLp);
                coverBadge.setText("COVER");
                coverBadge.setTextColor(0xFFFFFFFF);
                coverBadge.setTextSize(8f);
                coverBadge.setBackgroundResource(R.drawable.badge_bg);
                coverBadge.setPadding(dpToPx(6), dpToPx(3), dpToPx(6), dpToPx(3));
                frame.addView(coverBadge);
            }

            TextView deleteBtn = new TextView(this);
            FrameLayout.LayoutParams dLp = new FrameLayout.LayoutParams(dpToPx(22), dpToPx(22));
            dLp.gravity = android.view.Gravity.TOP | android.view.Gravity.END;
            dLp.setMargins(0, dpToPx(4), dpToPx(4), 0);
            deleteBtn.setLayoutParams(dLp);
            deleteBtn.setText("✕");
            deleteBtn.setTextColor(0xFFFFFFFF);
            deleteBtn.setTextSize(10f);
            deleteBtn.setGravity(android.view.Gravity.CENTER);
            deleteBtn.setBackgroundColor(0xAA000000);
            frame.addView(deleteBtn);

            deleteBtn.setOnClickListener(v -> {
                selectedPhotoUris.remove(idx);
                refreshPhotoGrid();
            });

            photoGrid.addView(frame);
        }

        if (btnStep1Next != null) {
            if (selectedPhotoUris.isEmpty()) {
                btnStep1Next.setText("Continue →");
            } else {
                btnStep1Next.setText(selectedPhotoUris.size() + " photo" + (selectedPhotoUris.size() > 1 ? "s" : "") + " selected — Continue →");
            }
        }
    }

    private boolean validateStep2() {
        boolean ok = true;
        if (tilTitle != null) tilTitle.setError(null);
        if (tilPrice != null) tilPrice.setError(null);
        if (tilAddress != null) tilAddress.setError(null);

        if (getText(etTitle).isEmpty()) { if (tilTitle != null) tilTitle.setError("Title is required"); ok = false; }
        if (getText(etPrice).isEmpty()) { if (tilPrice != null) tilPrice.setError("Price is required"); ok = false; }
        if (getText(etAddress).isEmpty()) { if (tilAddress != null) tilAddress.setError("Address is required"); ok = false; }
        if (selectedCategory.isEmpty()) {
            Toast.makeText(this, "Please select a property type", Toast.LENGTH_SHORT).show();
            ok = false;
        }
        return ok;
    }

    private boolean validateStep3() {
        boolean ok = true;
        if (tilBeds != null) tilBeds.setError(null);
        if (tilBaths != null) tilBaths.setError(null);
        if (tilSqft != null) tilSqft.setError(null);
        if (tilContactPhone != null) tilContactPhone.setError(null);

        if (getText(etBeds).isEmpty()) { if (tilBeds != null) tilBeds.setError("Required"); ok = false; }
        if (getText(etBaths).isEmpty()) { if (tilBaths != null) tilBaths.setError("Required"); ok = false; }
        if (getText(etSqft).isEmpty()) { if (tilSqft != null) tilSqft.setError("Required"); ok = false; }
        if (getText(etContactPhone).isEmpty()) {
            if (tilContactPhone != null) tilContactPhone.setError("Phone number is required");
            ok = false;
        } else if (getText(etContactPhone).replaceAll("[^0-9]", "").length() < 10) {
            if (tilContactPhone != null) tilContactPhone.setError("Enter a valid phone number");
            ok = false;
        }
        return ok;
    }

    private void populatePreview() {
        if (tvPreviewTitle != null) tvPreviewTitle.setText(getText(etTitle));
        if (tvPreviewAddress != null) tvPreviewAddress.setText(getText(etAddress));
        if (tvPreviewPrice != null) tvPreviewPrice.setText("$" + formatPrice(getText(etPrice)));

        if (ivPreviewCover != null && !selectedPhotoUris.isEmpty()) {
            Glide.with(this).load(selectedPhotoUris.get(0)).centerCrop().placeholder(R.drawable.property_1).into(ivPreviewCover);
        }
    }

    private void publishListing() {
        if (cbListingTerms == null || !cbListingTerms.isChecked()) {
            Toast.makeText(this, "Please confirm that you own or have authority to list this property", Toast.LENGTH_SHORT).show();
            return;
        }

        if (btnPublish != null) {
            btnPublish.setEnabled(false);
            btnPublish.setText("Preparing…");
        }

        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "anon";
        String email = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : "";

        SupabaseManager.ListingData data = new SupabaseManager.ListingData();
        data.ownerId = uid;
        data.ownerEmail = email;
        data.ownerPhone = getText(etContactPhone);
        data.title = getText(etTitle);
        data.price = getText(etPrice).replaceAll("[^0-9]", "");
        data.address = getText(etAddress);
        data.category = selectedCategory;
        data.beds = safeInt(getText(etBeds));
        data.baths = safeFloat(getText(etBaths));
        data.sqft = getText(etSqft);
        data.garages = safeInt(getText(etGarages));
        data.description = getText(etDescription);
        data.badge = "NEW LISTING";
        data.rating = 4.5f;

        supabase.uploadPropertyListing(data, selectedPhotoUris, new SupabaseManager.UploadCallback() {
            @Override public void onProgress(int photoIndex, int total) {
                if (btnPublish != null) btnPublish.setText("Uploading photo " + photoIndex + " of " + total + "…");
            }
            @Override public void onSuccess(String listingId) {
                Toast.makeText(UploadPropertyActivity.this, "🎉 Your listing is now live!", Toast.LENGTH_LONG).show();
                finish();
            }
            @Override public void onError(String error) {
                if (btnPublish != null) {
                    btnPublish.setEnabled(true);
                    btnPublish.setText("Publish Listing");
                }
                Toast.makeText(UploadPropertyActivity.this, "Upload failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void goToStep(int step) {
        boolean forward = step > currentStep;
        currentStep = step;

        String[] labels = {"", "Step 1 of 4 — Photos", "Step 2 of 4 — Basics", "Step 3 of 4 — Details", "Step 4 of 4 — Review & Publish"};
        if (tvStepLabel != null) tvStepLabel.setText(labels[step]);

        View[] steps = {layoutUploadStep1, layoutUploadStep2, layoutUploadStep3, layoutUploadStep4};
        View[] bars = {stepBar1, stepBar2, stepBar3, stepBar4};

        for (int i = 0; i < steps.length; i++) {
            int target = i + 1;
            if (steps[i] != null) {
                if (target == step) slideIn(steps[i], forward);
                else slideOut(steps[i], forward);
            }
            if (bars[i] != null) {
                bars[i].setBackgroundResource(target <= step ? R.drawable.step_dot_active : R.drawable.step_dot_inactive);
            }
        }
    }

    private void slideIn(View v, boolean fromRight) {
        v.setVisibility(View.VISIBLE);
        v.setAlpha(0f);
        v.setTranslationX(fromRight ? 350f : -350f);
        v.animate().translationX(0f).alpha(1f).setDuration(300).setInterpolator(new DecelerateInterpolator(1.4f)).start();
    }

    private void slideOut(View v, boolean toLeft) {
        if (v.getVisibility() == View.GONE) return;
        v.animate().translationX(toLeft ? -350f : 350f).alpha(0f).setDuration(260).setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> { v.setVisibility(View.GONE); v.setTranslationX(0f); }).start();
    }

    private String getText(TextInputEditText et) { return et != null && et.getText() != null ? et.getText().toString().trim() : ""; }
    private int safeInt(String s) { try { return Integer.parseInt(s.replaceAll("[^0-9]", "")); } catch (Exception e) { return 0; } }
    private float safeFloat(String s) { try { return Float.parseFloat(s.trim()); } catch (Exception e) { return 0f; } }
    private String formatPrice(String raw) { try { long val = Long.parseLong(raw.replaceAll("[^0-9]", "")); return String.format(Locale.US, "%,d", val); } catch (Exception e) { return raw; } }
    private int dpToPx(int dp) { return Math.round(dp * getResources().getDisplayMetrics().density); }

    @Override
    public void onBackPressed() {
        if (currentStep > 1) goToStep(currentStep - 1);
        else super.onBackPressed();
    }
}