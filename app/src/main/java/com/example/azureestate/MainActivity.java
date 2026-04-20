package com.example.azureestate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    // Nav tabs - 6 tabs
    private LinearLayout navDiscover, navFavorites, navSearch, navMessages, navAiChat, navProfile;
    private ImageView    ivNavDiscover, ivNavFavorites, ivNavSearch, ivNavMessages, ivNavAiChat, ivNavProfile;
    private TextView     tvNavDiscover, tvNavFavorites, tvNavSearch, tvNavMessages, tvNavAiChat, tvNavProfile;
    private TextView     tvAiChatBadge;  // Badge for AI Chat

    // FAB
    private FloatingActionButton fabUpload;

    // State
    private int currentTab = 0;   // 0=Home 1=Favorites 2=Search 3=AI Chat 4=Profile

    // Colors
    private static final int COLOR_ACTIVE   = 0xFF3DB8A8;
    private static final int COLOR_INACTIVE = 0xFF9BAABB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        setupNavClicks();
        setupFab();

        // Default tab — Home/Discover
        if (savedInstanceState == null) {
            selectTab(0);
        }
    }

    private void bindViews() {
        navDiscover   = findViewById(R.id.navDiscover);
        navFavorites  = findViewById(R.id.navFavorites);
        navSearch     = findViewById(R.id.navSearch);
        navMessages   = findViewById(R.id.navMessages);
        navAiChat     = findViewById(R.id.navAiChat);
        navProfile    = findViewById(R.id.navProfile);

        ivNavDiscover  = findViewById(R.id.ivNavDiscover);
        ivNavFavorites = findViewById(R.id.ivNavFavorites);
        ivNavSearch    = findViewById(R.id.ivNavSearch);
        ivNavMessages  = findViewById(R.id.ivNavMessages);
        ivNavAiChat    = findViewById(R.id.ivNavAiChat);
        ivNavProfile   = findViewById(R.id.ivNavProfile);

        tvNavDiscover  = findViewById(R.id.tvNavDiscover);
        tvNavFavorites = findViewById(R.id.tvNavFavorites);
        tvNavSearch    = findViewById(R.id.tvNavSearch);
        tvNavMessages  = findViewById(R.id.tvNavMessages);
        tvNavAiChat    = findViewById(R.id.tvNavAiChat);
        tvNavProfile   = findViewById(R.id.tvNavProfile);

        fabUpload = findViewById(R.id.fabUpload);
        
        // Try to find badge view (optional - create in XML if needed)
       // tvAiChatBadge = findViewById(R.id.tvAiChatBadge);
    }

    private void setupNavClicks() {
        navDiscover.setOnClickListener(v -> selectTab(0));
        navFavorites.setOnClickListener(v -> selectTab(1));
        navSearch.setOnClickListener(v -> selectTab(2));
        navMessages.setOnClickListener(v -> selectTab(3));
        navAiChat.setOnClickListener(v -> selectTab(4));
        navProfile.setOnClickListener(v -> selectTab(5));
    }

    private void setupFab() {
        if (fabUpload != null) {
            fabUpload.setOnClickListener(v -> {
                fabUpload.animate()
                        .scaleX(0.85f).scaleY(0.85f).setDuration(100)
                        .withEndAction(() ->
                                fabUpload.animate()
                                        .scaleX(1f).scaleY(1f)
                                        .setDuration(200)
                                        .setInterpolator(new OvershootInterpolator(2f))
                                        .start())
                        .start();

                fabUpload.postDelayed(() ->
                        startActivity(new Intent(this, UploadPropertyActivity.class)), 120);
            });

            fabUpload.setScaleX(0f);
            fabUpload.setScaleY(0f);
            fabUpload.animate()
                    .scaleX(1f).scaleY(1f)
                    .setDuration(400)
                    .setStartDelay(300)
                    .setInterpolator(new OvershootInterpolator(1.6f))
                    .start();
        }
    }

    public void selectTab(int tab) {
        if (tab == currentTab && getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainer) != null) return;

        currentTab = tab;
        Fragment fragment;
        switch (tab) {
            case 1:  fragment = new FavoritesFragment(); break;
            case 2:  fragment = new SearchFragment();    break;
            case 3:  fragment = new MessagesFragment();  break;
            case 4:  fragment = new AiChatFragment();    break;
            case 5:  fragment = new ProfileFragment();   break;
            default: fragment = new HomeFragment();      break;
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        ft.replace(R.id.fragmentContainer, fragment);
        ft.commit();

        updateNavUI(tab);
        
        // Clear badge when AI Chat tab is opened
        if (tab == 3) {
            showMessageBadge(0);
        }
    }

    private void updateNavUI(int selectedTab) {
        // Reset all to inactive
        setNavItem(ivNavDiscover,  tvNavDiscover,  false);
        setNavItem(ivNavFavorites, tvNavFavorites, false);
        setNavItem(ivNavSearch,    tvNavSearch,    false);
        setNavItem(ivNavMessages,  tvNavMessages,  false);
        setNavItem(ivNavAiChat,    tvNavAiChat,    false);
        setNavItem(ivNavProfile,   tvNavProfile,   false);

        // Activate selected
        switch (selectedTab) {
            case 0: setNavItem(ivNavDiscover,  tvNavDiscover,  true); break;
            case 1: setNavItem(ivNavFavorites, tvNavFavorites, true); break;
            case 2: setNavItem(ivNavSearch,    tvNavSearch,    true); break;
            case 3: setNavItem(ivNavMessages,  tvNavMessages,  true); break;
            case 4: setNavItem(ivNavAiChat,    tvNavAiChat,    true); break;
            case 5: setNavItem(ivNavProfile,   tvNavProfile,   true); break;
        }

        // Bounce the selected icon
        View[] icons = {ivNavDiscover, ivNavFavorites, ivNavSearch, ivNavMessages, ivNavAiChat, ivNavProfile};
        if (selectedTab < icons.length && icons[selectedTab] != null) {
            bounceIcon(icons[selectedTab]);
        }

        // Hide/show FAB: only visible on Home, Favorites, Search
        boolean showFab = (selectedTab == 0 || selectedTab == 1 || selectedTab == 2);
        if (fabUpload != null) {
            fabUpload.animate().scaleX(showFab ? 1f : 0f)
                    .scaleY(showFab ? 1f : 0f)
                    .setDuration(180)
                    .setInterpolator(new OvershootInterpolator(1.5f))
                    .start();
        }
    }

    private void setNavItem(ImageView icon, TextView label, boolean active) {
        if (icon == null || label == null) return;
        int color = active ? COLOR_ACTIVE : COLOR_INACTIVE;
        icon.setColorFilter(color);
        label.setTextColor(color);
        label.setTypeface(active
                ? android.graphics.Typeface.DEFAULT_BOLD
                : android.graphics.Typeface.DEFAULT);
    }

    private void bounceIcon(View icon) {
        icon.animate().scaleX(1.25f).scaleY(1.25f).setDuration(120)
                .withEndAction(() ->
                        icon.animate().scaleX(1f).scaleY(1f)
                                .setDuration(150)
                                .setInterpolator(new OvershootInterpolator(2f))
                                .start())
                .start();
    }

    // Show badge on AI Chat tab
    public void showMessageBadge(int count) {
        if (tvAiChatBadge != null) {
            if (count > 0 && currentTab != 3) {
                tvAiChatBadge.setText(count > 9 ? "9+" : String.valueOf(count));
                tvAiChatBadge.setVisibility(View.VISIBLE);
                bounceIcon(tvAiChatBadge);
            } else {
                tvAiChatBadge.setVisibility(View.GONE);
            }
        }
    }

    public void navigateToHome() {
        selectTab(0);
    }

    public void navigateToFavorites() {
        selectTab(1);
    }

    public void navigateToAiChat() {
        selectTab(4);
    }
}