package com.example.azureestate;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.slider.RangeSlider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignUpActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    // Steps
    private LinearLayout layoutStep1, layoutStep2, layoutStep3;
    private View dot1, dot2, dot3;
    private LinearLayout llOrDivider, llSocialButtons;
    private int currentStep = 1;

    // Step 1 fields
    private TextInputLayout tilFirstName, tilLastName, tilPhone;
    private TextInputEditText etFirstName, etLastName, etPhone;
    private TextView tvDobDisplay;
    private String selectedDob = "";

    // Step 2 fields
    private TextInputLayout tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etEmail, etPassword, etConfirmPassword;
    private TextView tvPasswordStrength;
    private View strengthBar1, strengthBar2, strengthBar3, strengthBar4;

    // Step 3 fields
    private TextView chipHouses, chipApartments, chipVillas;
    private RangeSlider sliderBudget;
    private TextView tvBudgetMin, tvBudgetMax;
    private TextInputLayout tilLocation;
    private TextInputEditText etLocation;
    private CheckBox cbTerms;
    private String selectedPropertyType = "";

    // Buttons
    private Button btnStep1Next, btnStep2Back, btnStep2Next;
    private Button btnStep3Back, btnSubmit;
    private Button btnGoogle;
    private TextView tvSignIn, tvContactSales;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        bindViews();
        setupStep1();
        setupStep2();
        setupStep3();
        setupSocialButtons();
        setupSignInLink();
        setupContactSales();
    }

    // ─────────────────────────────────────────────────────────────
    //  Bind all views
    // ─────────────────────────────────────────────────────────────
    private void bindViews() {
        layoutStep1 = findViewById(R.id.layoutStep1);
        layoutStep2 = findViewById(R.id.layoutStep2);
        layoutStep3 = findViewById(R.id.layoutStep3);
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);
        llOrDivider     = findViewById(R.id.llOrDivider);
        llSocialButtons = findViewById(R.id.llSocialButtons);

        // Step 1
        tilFirstName = findViewById(R.id.tilFirstName);
        tilLastName  = findViewById(R.id.tilLastName);
        tilPhone     = findViewById(R.id.tilPhone);
        etFirstName  = findViewById(R.id.etFirstName);
        etLastName   = findViewById(R.id.etLastName);
        etPhone      = findViewById(R.id.etPhone);
        tvDobDisplay = findViewById(R.id.tvDobDisplay);
        btnStep1Next = findViewById(R.id.btnStep1Next);

        // Step 2
        tilEmail           = findViewById(R.id.tilEmail);
        tilPassword        = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etEmail            = findViewById(R.id.etEmail);
        etPassword         = findViewById(R.id.etPassword);
        etConfirmPassword  = findViewById(R.id.etConfirmPassword);
        tvPasswordStrength = findViewById(R.id.tvPasswordStrength);
        strengthBar1       = findViewById(R.id.strengthBar1);
        strengthBar2       = findViewById(R.id.strengthBar2);
        strengthBar3       = findViewById(R.id.strengthBar3);
        strengthBar4       = findViewById(R.id.strengthBar4);
        btnStep2Back       = findViewById(R.id.btnStep2Back);
        btnStep2Next       = findViewById(R.id.btnStep2Next);

        // Step 3
        chipHouses      = findViewById(R.id.chipHouses);
        chipApartments  = findViewById(R.id.chipApartments);
        chipVillas      = findViewById(R.id.chipVillas);
        sliderBudget    = findViewById(R.id.sliderBudget);
        tvBudgetMin     = findViewById(R.id.tvBudgetMin);
        tvBudgetMax     = findViewById(R.id.tvBudgetMax);
        tilLocation     = findViewById(R.id.tilLocation);
        etLocation      = findViewById(R.id.etLocation);
        cbTerms         = findViewById(R.id.cbTerms);
        btnStep3Back    = findViewById(R.id.btnStep3Back);
        btnSubmit       = findViewById(R.id.btnSubmit);

        // Common
        btnGoogle      = findViewById(R.id.btnGoogle);
        tvSignIn       = findViewById(R.id.tvSignIn);
        tvContactSales = findViewById(R.id.tvContactSales);
    }

    // ─────────────────────────────────────────────────────────────
    //  Step 1 — Personal Info
    // ─────────────────────────────────────────────────────────────
    private void setupStep1() {
        // Date picker
        findViewById(R.id.tilDob).setOnClickListener(v -> showDatePicker());
        tvDobDisplay.setOnClickListener(v -> showDatePicker());
        findViewById(R.id.ivCalendar).setOnClickListener(v -> showDatePicker());

        btnStep1Next.setOnClickListener(v -> {
            if (validateStep1()) {
                goToStep(2);
            }
        });
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        // Default to 30 years ago
        cal.add(Calendar.YEAR, -30);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                R.style.DatePickerTheme,
                (view, year, month, dayOfMonth) -> {
                    selectedDob = String.format(Locale.US, "%02d / %02d / %d",
                            month + 1, dayOfMonth, year);
                    tvDobDisplay.setText(selectedDob);
                    tvDobDisplay.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        // Max date: must be 18+
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, -18);
        dialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        dialog.show();
    }

    private boolean validateStep1() {
        boolean ok = true;
        clearErrors(tilFirstName, tilLastName, tilPhone);

        String firstName = text(etFirstName);
        String lastName  = text(etLastName);
        String phone     = text(etPhone);

        if (firstName.isEmpty()) {
            tilFirstName.setError("First name is required");
            ok = false;
        }
        if (lastName.isEmpty()) {
            tilLastName.setError("Last name is required");
            ok = false;
        }
        if (phone.isEmpty()) {
            tilPhone.setError("Phone number is required");
            ok = false;
        } else if (phone.replaceAll("[^0-9]", "").length() < 10) {
            tilPhone.setError("Enter a valid phone number");
            ok = false;
        }
        if (selectedDob.isEmpty()) {
            Toast.makeText(this, "Please select your date of birth", Toast.LENGTH_SHORT).show();
            ok = false;
        }
        return ok;
    }

    // ─────────────────────────────────────────────────────────────
    //  Step 2 — Account Setup
    // ─────────────────────────────────────────────────────────────
    private void setupStep2() {
        // Live password strength
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int i, int b, int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                updatePasswordStrength(s.toString());
            }
        });

        btnStep2Back.setOnClickListener(v -> goToStep(1));
        btnStep2Next.setOnClickListener(v -> {
            if (validateStep2()) goToStep(3);
        });
    }

    private void updatePasswordStrength(String password) {
        int score = 0;
        if (password.length() >= 8)                             score++;
        if (password.matches(".*[A-Z].*"))                      score++;
        if (password.matches(".*[0-9].*"))                      score++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]].*")) score++;

        int activeColor   = ContextCompat.getColor(this, R.color.teal_primary);
        int warnColor     = 0xFFFFA726;  // amber
        int dangerColor   = 0xFFE53935;  // red
        int inactiveColor = ContextCompat.getColor(this, R.color.divider);

        View[] bars = {strengthBar1, strengthBar2, strengthBar3, strengthBar4};
        int fillColor;
        String label;
        switch (score) {
            case 1:  fillColor = dangerColor; label = "Weak";   break;
            case 2:  fillColor = warnColor;   label = "Fair";   break;
            case 3:  fillColor = activeColor; label = "Good";   break;
            case 4:  fillColor = activeColor; label = "Strong"; break;
            default: fillColor = inactiveColor; label = "";     break;
        }

        for (int i = 0; i < bars.length; i++) {
            bars[i].setBackgroundColor(i < score ? fillColor : inactiveColor);
        }
        tvPasswordStrength.setText(label);
        tvPasswordStrength.setTextColor(fillColor);
    }

    private boolean validateStep2() {
        boolean ok = true;
        clearErrors(tilEmail, tilPassword, tilConfirmPassword);

        String email    = text(etEmail);
        String password = text(etPassword);
        String confirm  = text(etConfirmPassword);

        if (email.isEmpty()) {
            tilEmail.setError("Email is required"); ok = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email"); ok = false;
        }
        if (password.isEmpty()) {
            tilPassword.setError("Password is required"); ok = false;
        } else if (password.length() < 8) {
            tilPassword.setError("Minimum 8 characters"); ok = false;
        }
        if (confirm.isEmpty()) {
            tilConfirmPassword.setError("Please confirm your password"); ok = false;
        } else if (!confirm.equals(password)) {
            tilConfirmPassword.setError("Passwords do not match"); ok = false;
        }
        return ok;
    }

    // ─────────────────────────────────────────────────────────────
    //  Step 3 — Preferences
    // ─────────────────────────────────────────────────────────────
    private void setupStep3() {
        // Property type chips
        View.OnClickListener chipClick = v -> {
            resetChips();
            v.setBackgroundResource(R.drawable.chip_active);
            ((TextView) v).setTextColor(ContextCompat.getColor(this, R.color.navy_dark));
            selectedPropertyType = ((TextView) v).getText().toString();
            animateChipSelect(v);
        };
        chipHouses.setOnClickListener(chipClick);
        chipApartments.setOnClickListener(chipClick);
        chipVillas.setOnClickListener(chipClick);

        // Budget range slider
        sliderBudget.addOnChangeListener((slider, value, fromUser) -> {
            float min = slider.getValues().get(0);
            float max = slider.getValues().get(1);
            tvBudgetMin.setText(formatBudget(min));
            tvBudgetMax.setText(formatBudget(max));
        });

        btnStep3Back.setOnClickListener(v -> goToStep(2));
        btnSubmit.setOnClickListener(v -> {
            if (validateStep3()) submitRegistration();
        });
    }

    private void resetChips() {
        chipHouses.setBackgroundResource(R.drawable.chip_inactive);
        chipApartments.setBackgroundResource(R.drawable.chip_inactive);
        chipVillas.setBackgroundResource(R.drawable.chip_inactive);
        chipHouses.setTextColor(0xFF7A8899);
        chipApartments.setTextColor(0xFF7A8899);
        chipVillas.setTextColor(0xFF7A8899);
    }

    private void animateChipSelect(View v) {
        v.animate().scaleX(1.06f).scaleY(1.06f).setDuration(100)
                .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(100).start())
                .start();
    }

    private String formatBudget(float value) {
        if (value >= 1_000_000) return String.format(Locale.US, "$%.0fM", value / 1_000_000);
        return String.format(Locale.US, "$%.0fK", value / 1_000);
    }

    private boolean validateStep3() {
        if (!cbTerms.isChecked()) {
            Toast.makeText(this,
                    "Please accept the Terms of Service to continue",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // ─────────────────────────────────────────────────────────────
    //  Step navigation with slide animation
    // ─────────────────────────────────────────────────────────────
    private void goToStep(int step) {
        boolean forward = step > currentStep;
        currentStep = step;

        // Animate out current, animate in next
        LinearLayout[] steps = {layoutStep1, layoutStep2, layoutStep3};
        for (int i = 0; i < steps.length; i++) {
            int targetStep = i + 1;
            if (targetStep == step) {
                slideIn(steps[i], forward);
            } else {
                slideOut(steps[i], forward);
            }
        }

        updateStepDots(step);

        // Only show OR/social on step 1
        boolean showSocial = (step == 1);
        llOrDivider.setVisibility(showSocial ? View.VISIBLE : View.GONE);
        llSocialButtons.setVisibility(showSocial ? View.VISIBLE : View.GONE);
    }

    private void slideIn(View v, boolean fromRight) {
        float startX = fromRight ? 400f : -400f;
        v.setVisibility(View.VISIBLE);
        v.setAlpha(0f);
        v.setTranslationX(startX);
        v.animate()
                .translationX(0f)
                .alpha(1f)
                .setDuration(320)
                .setInterpolator(new DecelerateInterpolator(1.5f))
                .start();
    }

    private void slideOut(View v, boolean toLeft) {
        if (v.getVisibility() == View.GONE) return;
        float endX = toLeft ? -400f : 400f;
        v.animate()
                .translationX(endX)
                .alpha(0f)
                .setDuration(280)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    v.setVisibility(View.GONE);
                    v.setTranslationX(0f);
                })
                .start();
    }

    private void updateStepDots(int step) {
        // Animate dots
        animateDot(dot1, step >= 1);
        animateDot(dot2, step >= 2);
        animateDot(dot3, step >= 3);

        dot1.setBackgroundResource(step == 1 ? R.drawable.step_dot_active : R.drawable.step_dot_complete);
        dot2.setBackgroundResource(step == 2 ? R.drawable.step_dot_active :
                step > 2 ? R.drawable.step_dot_complete : R.drawable.step_dot_inactive);
        dot3.setBackgroundResource(step == 3 ? R.drawable.step_dot_active : R.drawable.step_dot_inactive);
    }

    private void animateDot(View dot, boolean activate) {
        dot.animate().scaleX(activate ? 1.15f : 1f).scaleY(activate ? 1.15f : 1f)
                .setDuration(200).start();
    }

    // ─────────────────────────────────────────────────────────────
    //  Final submit
    // ─────────────────────────────────────────────────────────────
    private void submitRegistration() {
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Joining The Estate...");

        String email = text(etEmail);
        String password = text(etPassword);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Join The Estate");
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(this,
                                "Welcome to The Estate, " + text(etFirstName) + "!",
                                Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    } else {
                        Toast.makeText(SignUpActivity.this, "Registration failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ─────────────────────────────────────────────────────────────
    //  Social buttons
    // ─────────────────────────────────────────────────────────────
    private void setupSocialButtons() {
        btnGoogle.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    } else {
                        Toast.makeText(SignUpActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ─────────────────────────────────────────────────────────────
    //  "Already a member? Sign In" spannable link
    // ─────────────────────────────────────────────────────────────
    private void setupSignInLink() {
        String full      = "Already a member? Sign In";
        String clickable = "Sign In";
        SpannableString spannable = new SpannableString(full);
        int start = full.indexOf(clickable);
        int end   = start + clickable.length();

        spannable.setSpan(
                new ForegroundColorSpan(ContextCompat.getColor(this, R.color.teal_primary)),
                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannable.setSpan(new ClickableSpan() {
            @Override public void onClick(@NonNull View widget) {
                finish(); // go back to LoginActivity
            }
            @Override public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                ds.setColor(ContextCompat.getColor(SignUpActivity.this, R.color.teal_primary));
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvSignIn.setText(spannable);
        tvSignIn.setMovementMethod(LinkMovementMethod.getInstance());
        tvSignIn.setHighlightColor(android.graphics.Color.TRANSPARENT);
    }

    private void setupContactSales() {
        tvContactSales.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, ContactSalesActivity.class);
            startActivity(intent);
        });
    }

    // ─────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────
    private String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void clearErrors(TextInputLayout... tils) {
        for (TextInputLayout til : tils) til.setError(null);
    }

    @Override
    public void onBackPressed() {
        if (currentStep > 1) {
            goToStep(currentStep - 1);
        } else {
            super.onBackPressed();
        }
    }
}