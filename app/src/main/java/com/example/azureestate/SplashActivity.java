package com.example.azureestate;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2800; // ms

    private CardView cardIcon;
    private TextView tvBrandName;
    private TextView tvTagline;
    private View divider;
    private TextView tvSubtitle;
    private TextView tvMember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full screen immersive
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );

        setContentView(R.layout.activity_splash);

        bindViews();
        startAnimations();
        scheduleTransition();
    }

    private void bindViews() {
        cardIcon    = findViewById(R.id.cardIcon);
        tvBrandName = findViewById(R.id.tvBrandName);
        tvTagline   = findViewById(R.id.tvTagline);
        divider     = findViewById(R.id.divider);
        tvSubtitle  = findViewById(R.id.tvSubtitle);
        tvMember    = findViewById(R.id.tvMember);

        // Start invisible
        cardIcon.setAlpha(0f);
        tvBrandName.setAlpha(0f);
        tvTagline.setAlpha(0f);
        divider.setAlpha(0f);
        tvSubtitle.setAlpha(0f);
        tvMember.setAlpha(0f);

        cardIcon.setScaleX(0.6f);
        cardIcon.setScaleY(0.6f);
    }

    private void startAnimations() {
        // Icon: scale + fade
        ObjectAnimator iconScaleX = ObjectAnimator.ofFloat(cardIcon, "scaleX", 0.6f, 1f);
        ObjectAnimator iconScaleY = ObjectAnimator.ofFloat(cardIcon, "scaleY", 0.6f, 1f);
        ObjectAnimator iconAlpha  = ObjectAnimator.ofFloat(cardIcon, "alpha", 0f, 1f);
        iconScaleX.setDuration(500);
        iconScaleY.setDuration(500);
        iconAlpha.setDuration(500);

        AnimatorSet iconAnim = new AnimatorSet();
        iconAnim.playTogether(iconScaleX, iconScaleY, iconAlpha);
        iconAnim.setStartDelay(100);

        // Brand name fade + slide up
        tvBrandName.setTranslationY(20f);
        ObjectAnimator nameAlpha   = ObjectAnimator.ofFloat(tvBrandName, "alpha", 0f, 1f);
        ObjectAnimator nameTranslY = ObjectAnimator.ofFloat(tvBrandName, "translationY", 20f, 0f);
        nameAlpha.setDuration(450);
        nameTranslY.setDuration(450);
        AnimatorSet nameAnim = new AnimatorSet();
        nameAnim.playTogether(nameAlpha, nameTranslY);
        nameAnim.setStartDelay(500);

        // Tagline fade
        ObjectAnimator taglineAlpha = ObjectAnimator.ofFloat(tvTagline, "alpha", 0f, 1f);
        taglineAlpha.setDuration(400);
        taglineAlpha.setStartDelay(750);

        // Divider widen + fade
        ObjectAnimator divAlpha = ObjectAnimator.ofFloat(divider, "alpha", 0f, 1f);
        divAlpha.setDuration(400);
        divAlpha.setStartDelay(950);

        // Subtitle fade
        ObjectAnimator subAlpha = ObjectAnimator.ofFloat(tvSubtitle, "alpha", 0f, 1f);
        subAlpha.setDuration(400);
        subAlpha.setStartDelay(1100);

        // Member text fade
        ObjectAnimator memberAlpha = ObjectAnimator.ofFloat(tvMember, "alpha", 0f, 1f);
        memberAlpha.setDuration(500);
        memberAlpha.setStartDelay(1400);

        // Play all
        AnimatorSet masterSet = new AnimatorSet();
        masterSet.playTogether(
                iconAnim, nameAnim,
                taglineAlpha, divAlpha, subAlpha, memberAlpha
        );
        masterSet.start();
    }

    private void scheduleTransition() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            Intent intent;
            if (currentUser != null) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, SPLASH_DURATION);
    }
}