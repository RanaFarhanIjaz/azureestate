package com.example.azureestate;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.NumberFormat;
import java.util.Locale;

public class PricePredictorActivity extends AppCompatActivity {

    // ── Extracted weights from trained Linear Regression (StandardScaler + LR) ──
    private static final double INTERCEPT = 4_706_527.385;

    // Features: area, bedrooms, bathrooms, stories, mainroad, guestroom,
    //           basement, hotwaterheating, airconditioning, parking, prefarea, furnishingstatus
    private static final double[] COEF = {
            519288.130, 58690.918, 523153.383, 348177.114,
            128115.928, 89357.646, 188462.049, 150570.028,
            362446.186, 192786.986, 266661.049, 158183.270
    };
    private static final double[] MEANS = {
            5154.144, 2.959, 1.266, 1.782,
            0.858, 0.179, 0.358, 0.050,
            0.307, 0.686, 0.234, 0.940
    };
    private static final double[] STDS = {
            2201.784, 0.747, 0.477, 0.857,
            0.349, 0.383, 0.479, 0.219,
            0.461, 0.854, 0.423, 0.752
    };

    // UI
    private EditText etArea;
    private SeekBar sbBedrooms, sbBathrooms, sbStories, sbParking;
    private TextView tvBedrooms, tvBathrooms, tvStories, tvParking;
    private Switch swMainRoad, swGuestRoom, swBasement, swHotWater, swAC, swPrefArea;
    private Spinner spFurnishing;
    private Button btnPredict;
    private TextView tvResult, tvResultLabel;
    private View resultCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_predictor);

        bindViews();
        setupSeekBars();
        setupSpinner();

        // Back button
        findViewById(R.id.ivBack).setOnClickListener(v -> onBackPressed());

        btnPredict.setOnClickListener(v -> predict());
    }

    private void bindViews() {
        etArea         = findViewById(R.id.etArea);
        sbBedrooms     = findViewById(R.id.sbBedrooms);
        sbBathrooms    = findViewById(R.id.sbBathrooms);
        sbStories      = findViewById(R.id.sbStories);
        sbParking      = findViewById(R.id.sbParking);
        tvBedrooms     = findViewById(R.id.tvBedrooms);
        tvBathrooms    = findViewById(R.id.tvBathrooms);
        tvStories      = findViewById(R.id.tvStories);
        tvParking      = findViewById(R.id.tvParking);
        swMainRoad     = findViewById(R.id.swMainRoad);
        swGuestRoom    = findViewById(R.id.swGuestRoom);
        swBasement     = findViewById(R.id.swBasement);
        swHotWater     = findViewById(R.id.swHotWater);
        swAC           = findViewById(R.id.swAC);
        swPrefArea     = findViewById(R.id.swPrefArea);
        spFurnishing   = findViewById(R.id.spFurnishing);
        btnPredict     = findViewById(R.id.btnPredict);
        tvResult       = findViewById(R.id.tvResult);
        tvResultLabel  = findViewById(R.id.tvResultLabel);
        resultCard     = findViewById(R.id.resultCard);
    }

    private void setupSeekBars() {
        setupBar(sbBedrooms, tvBedrooms, 1, 6, "Bedrooms");
        setupBar(sbBathrooms, tvBathrooms, 1, 4, "Bathrooms");
        setupBar(sbStories, tvStories, 1, 4, "Stories");
        setupBar(sbParking, tvParking, 0, 3, "Parking");
    }

    private void setupBar(SeekBar sb, TextView tv, int min, int max, String label) {
        sb.setMax(max - min);
        sb.setProgress(0);
        tv.setText(label + ": " + min);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s, int p, boolean f) {
                tv.setText(label + ": " + (p + min));
            }
            public void onStartTrackingTouch(SeekBar s) {}
            public void onStopTrackingTouch(SeekBar s) {}
        });
    }

    private void setupSpinner() {
        String[] options = {"Furnished", "Semi-Furnished", "Unfurnished"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFurnishing.setAdapter(adapter);
    }

    private void predict() {
        // --- Parse inputs ---
        String areaStr = etArea.getText().toString().trim();
        if (areaStr.isEmpty()) {
            etArea.setError("Please enter the area");
            etArea.requestFocus();
            return;
        }

        double area      = Double.parseDouble(areaStr);
        double bedrooms  = sbBedrooms.getProgress()  + 1;
        double bathrooms = sbBathrooms.getProgress() + 1;
        double stories   = sbStories.getProgress()   + 1;
        double parking   = sbParking.getProgress();  // 0-based

        double mainroad        = swMainRoad.isChecked()  ? 1 : 0;
        double guestroom       = swGuestRoom.isChecked() ? 1 : 0;
        double basement        = swBasement.isChecked()  ? 1 : 0;
        double hotwaterheating = swHotWater.isChecked()  ? 1 : 0;
        double airconditioning = swAC.isChecked()        ? 1 : 0;
        double prefarea        = swPrefArea.isChecked()  ? 1 : 0;

        // furnishing: Furnished=2, Semi-Furnished=1, Unfurnished=0
        double furnishing = 2 - spFurnishing.getSelectedItemPosition();

        double[] rawInput = {
                area, bedrooms, bathrooms, stories,
                mainroad, guestroom, basement, hotwaterheating,
                airconditioning, parking, prefarea, furnishing
        };

        // --- Standardise + predict ---
        double price = INTERCEPT;
        for (int i = 0; i < rawInput.length; i++) {
            double scaled = (rawInput[i] - MEANS[i]) / STDS[i];
            price += COEF[i] * scaled;
        }

        // --- Display result ---
        price = Math.max(price, 500_000); // floor
        String formatted = "Rs. " + NumberFormat.getNumberInstance(Locale.US)
                .format((long) price);

        tvResult.setText(formatted);
        tvResultLabel.setText("Estimated Market Price");

        resultCard.setVisibility(View.VISIBLE);
        resultCard.setAlpha(0f);
        resultCard.animate().alpha(1f).translationY(0).setDuration(400).start();

        // Pulse the button
        ObjectAnimator.ofFloat(btnPredict, "scaleX", 1f, 0.95f, 1f)
                .setDuration(200).start();
    }
}
