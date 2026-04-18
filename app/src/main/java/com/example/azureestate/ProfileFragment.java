package com.example.azureestate;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseUser currentUser;

    // Header views
    private ImageView ivProfileImage, ivSettings;
    private FrameLayout ivEditPhoto;
    private TextView tvFullName, tvEmail;
    private Button btnEditProfile, btnSignOut;

    // Row containers (using the redesigned item_profile_row.xml)
    private View rowMyProperties, rowWishlist, rowSavedSearches, rowMessages;
    private View rowNotifications, rowPayment, rowSettings;
    private View rowPrivacy, rowTerms, rowHelp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        try {
            return inflater.inflate(R.layout.fragment_profile, container, false);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error loading profile", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            mAuth = FirebaseAuth.getInstance();
            currentUser = mAuth.getCurrentUser();

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                    GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
            mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

            initViews(view);
            setupRows();
            setupClickListeners();
            displayUserData();
            animateEntrance(view);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Init error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ─────────────────────────────────────────────────────────────
    private void initViews(View view) {
        ivProfileImage = view.findViewById(R.id.ivProfileImage);
        ivSettings     = view.findViewById(R.id.ivSettings);
        ivEditPhoto    = view.findViewById(R.id.ivEditPhoto);
        tvFullName     = view.findViewById(R.id.tvFullName);
        tvEmail        = view.findViewById(R.id.tvEmail);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnSignOut     = view.findViewById(R.id.btnSignOut);

        rowMyProperties  = view.findViewById(R.id.rowMyProperties);
        rowWishlist      = view.findViewById(R.id.rowWishlist);
        rowSavedSearches = view.findViewById(R.id.rowSavedSearches);
        rowMessages      = view.findViewById(R.id.rowMessages);
        rowNotifications = view.findViewById(R.id.rowNotifications);
        rowPayment       = view.findViewById(R.id.rowPayment);
        rowSettings      = view.findViewById(R.id.rowSettings);
        rowPrivacy       = view.findViewById(R.id.rowPrivacy);
        rowTerms         = view.findViewById(R.id.rowTerms);
        rowHelp          = view.findViewById(R.id.rowHelp);
    }

    // ─────────────────────────────────────────────────────────────
    //  Configure each row's icon + label using the shared layout
    // ─────────────────────────────────────────────────────────────
    private void setupRows() {
        configRow(rowMyProperties,  R.drawable.ic_house,         "My Properties",  null);
        configRow(rowWishlist,       R.drawable.ic_favorite,      "Wishlist",       "28");
        configRow(rowSavedSearches,  R.drawable.ic_search,        "Saved Searches", null);
        configRow(rowMessages,       R.drawable.ic_message,       "Messages",       "3");
        configRow(rowNotifications,  R.drawable.ic_notifications, "Notifications",  null);
        configRow(rowPayment,        R.drawable.ic_payment,       "Payment Methods",null);
        configRow(rowSettings,       R.drawable.ic_settings,      "Settings",       null);
        configRow(rowPrivacy,        R.drawable.ic_privacy,       "Privacy Policy", null);
        configRow(rowTerms,          R.drawable.ic_terms,         "Terms of Service",null);
        configRow(rowHelp,           R.drawable.ic_help,          "Help Center",    null);
    }

    /**
     * Populates a profile row view inflated from item_profile_row.xml
     * @param rowView  the included row view
     * @param iconRes  drawable resource for the icon
     * @param label    text label
     * @param badge    optional badge count string, or null to hide badge
     */
    private void configRow(View rowView, int iconRes, String label, @Nullable String badge) {
        if (rowView == null) return;

        ImageView icon    = rowView.findViewById(R.id.ivRowIcon);
        TextView  tvLabel = rowView.findViewById(R.id.tvRowLabel);
        TextView  tvBadge = rowView.findViewById(R.id.tvRowBadge);

        if (icon    != null) icon.setImageResource(iconRes);
        if (tvLabel != null) tvLabel.setText(label);

        if (tvBadge != null) {
            if (badge != null && !badge.isEmpty()) {
                tvBadge.setText(badge);
                tvBadge.setVisibility(View.VISIBLE);
            } else {
                tvBadge.setVisibility(View.GONE);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    private void setupClickListeners() {
        safe(ivSettings,   () -> Toast.makeText(getContext(), "Settings coming soon", Toast.LENGTH_SHORT).show());
        safe(ivEditPhoto,  () -> Toast.makeText(getContext(), "Change photo coming soon", Toast.LENGTH_SHORT).show());
        safe(btnEditProfile,() -> Toast.makeText(getContext(), "Edit profile coming soon", Toast.LENGTH_SHORT).show());

        safe(rowMyProperties,  () -> Toast.makeText(getContext(), "My Properties", Toast.LENGTH_SHORT).show());
        safe(rowWishlist, () -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToFavorites();
            } else {
                Toast.makeText(getContext(), "Wishlist", Toast.LENGTH_SHORT).show();
            }
        });
        safe(rowSavedSearches, () -> Toast.makeText(getContext(), "Saved Searches", Toast.LENGTH_SHORT).show());
        safe(rowMessages,      () -> Toast.makeText(getContext(), "Messages", Toast.LENGTH_SHORT).show());
        safe(rowNotifications, () -> Toast.makeText(getContext(), "Notifications", Toast.LENGTH_SHORT).show());
        safe(rowPayment,       () -> Toast.makeText(getContext(), "Payment Methods", Toast.LENGTH_SHORT).show());
        safe(rowSettings,      () -> Toast.makeText(getContext(), "Settings", Toast.LENGTH_SHORT).show());
        safe(rowPrivacy,       () -> Toast.makeText(getContext(), "Privacy Policy", Toast.LENGTH_SHORT).show());
        safe(rowTerms,         () -> Toast.makeText(getContext(), "Terms of Service", Toast.LENGTH_SHORT).show());
        safe(rowHelp,          () -> Toast.makeText(getContext(), "Help Center", Toast.LENGTH_SHORT).show());

        if (btnSignOut != null) btnSignOut.setOnClickListener(v -> signOut());
    }

    /** Null-safe click listener helper */
    private void safe(@Nullable View view, Runnable action) {
        if (view != null) view.setOnClickListener(v -> {
            // Ripple scale feedback
            v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80)
                    .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(80).start())
                    .start();
            action.run();
        });
    }

    // ─────────────────────────────────────────────────────────────
    private void displayUserData() {
        try {
            if (currentUser != null) {
                String name  = currentUser.getDisplayName();
                String email = currentUser.getEmail();
                tvFullName.setText(name  != null && !name.isEmpty()  ? name  : "Estate Member");
                tvEmail.setText(email != null && !email.isEmpty() ? email : "No email");
                ivProfileImage.setImageResource(R.drawable.ic_avatar_placeholder);
            } else {
                tvFullName.setText("Guest User");
                tvEmail.setText("Not signed in");
                ivProfileImage.setImageResource(R.drawable.ic_avatar_placeholder);
            }
        } catch (Exception e) {
            e.printStackTrace();
            tvFullName.setText("User");
            tvEmail.setText("—");
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Staggered entrance animation
    // ─────────────────────────────────────────────────────────────
    private void animateEntrance(View root) {
        try {
          //  View hero    = root.findViewById(R.id.heroContainer);
            // Fade in entire scroll content
            root.setAlpha(0f);
            root.animate().alpha(1f).setDuration(350).setStartDelay(50).start();
        } catch (Exception ignored) {}
    }

    // ─────────────────────────────────────────────────────────────
    private void signOut() {
        if (btnSignOut != null) {
            btnSignOut.setEnabled(false);
            btnSignOut.setText("Signing out…");
        }
        if (mAuth != null) mAuth.signOut();
        if (mGoogleSignInClient != null) {
            mGoogleSignInClient.signOut().addOnCompleteListener(task -> navigateToLogin());
        } else {
            navigateToLogin();
        }
    }

    private void navigateToLogin() {
        try {
            if (btnSignOut != null) {
                btnSignOut.setEnabled(true);
                btnSignOut.setText("Sign Out");
            }
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (mAuth != null && mAuth.getCurrentUser() != null) {
                currentUser = mAuth.getCurrentUser();
                displayUserData();
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAuth = null;
        mGoogleSignInClient = null;
        currentUser = null;
    }
}