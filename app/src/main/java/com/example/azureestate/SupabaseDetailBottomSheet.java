package com.example.azureestate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import java.text.NumberFormat;
import java.util.Locale;

public class SupabaseDetailBottomSheet extends BottomSheetDialogFragment {

    private SupabaseManager.ListingData listing;

    public static SupabaseDetailBottomSheet newInstance(SupabaseManager.ListingData listing) {
        SupabaseDetailBottomSheet s = new SupabaseDetailBottomSheet();
        s.listing = listing;
        return s;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_property_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (listing == null) return;

        // Expand to full height
        View parent = (View) view.getParent();
        if (parent != null) {
            BottomSheetBehavior<View> b = BottomSheetBehavior.from(parent);
            b.setState(BottomSheetBehavior.STATE_EXPANDED);
            b.setSkipCollapsed(false);
        }

        // Cover image from Supabase Storage
        ImageView ivImage = view.findViewById(R.id.ivDetailImage);
        String cover = listing.getCoverPhotoUrl();
        if (cover != null && ivImage != null) {
            Glide.with(this).load(cover)
                    .transition(DrawableTransitionOptions.withCrossFade(300))
                    .centerCrop().into(ivImage);
        }

        // Texts
        setText(view, R.id.tvDetailTitle,   listing.title);
        setText(view, R.id.tvDetailAddress, listing.address);
        setText(view, R.id.tvDetailPrice,   listing.getFormattedPrice());
        setText(view, R.id.tvDetailBeds,    String.valueOf(listing.beds));
        setText(view, R.id.tvDetailBaths,   String.valueOf(listing.baths));
        setText(view, R.id.tvDetailSqft,    listing.sqft);
        setText(view, R.id.tvDetailGarages, String.valueOf(listing.garages));
        setText(view, R.id.tvDescription,   listing.description);
        setText(view, R.id.tvAgentName,     listing.ownerEmail);
        setText(view, R.id.tvAgentTitle,    "Property Owner");

        // Exclusive tag
        TextView tag = view.findViewById(R.id.tvExclusiveTag);
        if (tag != null) {
            tag.setText(listing.badge != null && !listing.badge.isEmpty()
                    ? listing.badge : "LISTING");
            tag.setVisibility(View.VISIBLE);
        }

        // Get Directions
        View directions = view.findViewById(R.id.btnOpenMap);
        if (directions != null) {
            directions.setOnClickListener(v -> {
                Uri mapUri = Uri.parse("geo:0,0?q=" + Uri.encode(listing.address));
                Intent i = new Intent(Intent.ACTION_VIEW, mapUri);
                i.setPackage("com.google.android.apps.maps");
                if (i.resolveActivity(requireActivity().getPackageManager()) != null)
                    startActivity(i);
                else startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://maps.google.com/?q=" + Uri.encode(listing.address))));
            });
        }

        // Contact Agent → open chat
        Button btnContact = view.findViewById(R.id.btnContactAgent);
        if (btnContact != null) {
            btnContact.setOnClickListener(v -> {
                String myUid = FirebaseAuth.getInstance().getCurrentUser() != null
                        ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
                String chatId = ChatRoomActivity.buildChatId(myUid, listing.ownerId, listing.id);

                Intent intent = new Intent(requireContext(), ChatRoomActivity.class);
                intent.putExtra(ChatRoomActivity.EXTRA_CHAT_ID,         chatId);
                intent.putExtra(ChatRoomActivity.EXTRA_OTHER_USER_ID,   listing.ownerId);
                intent.putExtra(ChatRoomActivity.EXTRA_OTHER_USER_NAME, listing.ownerEmail);
                intent.putExtra(ChatRoomActivity.EXTRA_PROPERTY_TITLE,  listing.title);
                intent.putExtra(ChatRoomActivity.EXTRA_PROPERTY_PRICE,  listing.getFormattedPrice());
                intent.putExtra(ChatRoomActivity.EXTRA_OWNER_PHONE,     listing.ownerPhone);
                startActivity(intent);
            });
        }

        // Schedule Tour → call
        Button btnTour = view.findViewById(R.id.btnScheduleTour);
        if (btnTour != null) {
            btnTour.setOnClickListener(v -> {
                if (listing.ownerPhone != null && !listing.ownerPhone.isEmpty()) {
                    Intent call = new Intent(Intent.ACTION_DIAL);
                    call.setData(Uri.parse("tel:" + listing.ownerPhone.replaceAll("[^+0-9]","")));
                    startActivity(call);
                } else {
                    Toast.makeText(requireContext(), "No phone number available",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        setupMortgageCalculator(view);
    }

    private void setupMortgageCalculator(View view) {
        EditText etDownPct = view.findViewById(R.id.etCalcDownPayment);
        EditText etInterest = view.findViewById(R.id.etCalcInterest);
        EditText etTerm = view.findViewById(R.id.etCalcTerm);
        EditText etIncome = view.findViewById(R.id.etCalcIncome);

        if (etDownPct == null) return;

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { calculateMortgage(view); }
        };

        etDownPct.addTextChangedListener(watcher);
        etInterest.addTextChangedListener(watcher);
        etTerm.addTextChangedListener(watcher);
        etIncome.addTextChangedListener(watcher);

        // Initial calculation
        calculateMortgage(view);
    }

    private void calculateMortgage(View view) {
        try {
            EditText etDownPct = view.findViewById(R.id.etCalcDownPayment);
            EditText etInterest = view.findViewById(R.id.etCalcInterest);
            EditText etTerm = view.findViewById(R.id.etCalcTerm);
            EditText etIncome = view.findViewById(R.id.etCalcIncome);

            double price = 0;
            try {
                price = Double.parseDouble(listing.price.replaceAll("[^0-9.]", ""));
            } catch (Exception e) {}
            double downPct = Double.parseDouble(etDownPct.getText().toString().isEmpty() ? "0" : etDownPct.getText().toString());
            double interest = Double.parseDouble(etInterest.getText().toString().isEmpty() ? "0" : etInterest.getText().toString());
            int years = Integer.parseInt(etTerm.getText().toString().isEmpty() ? "0" : etTerm.getText().toString());
            double income = Double.parseDouble(etIncome.getText().toString().isEmpty() ? "0" : etIncome.getText().toString());

            double downAmt = price * (downPct / 100.0);
            double loanAmt = price - downAmt;

            double monthlyRate = (interest / 100.0) / 12.0;
            int nPayments = years * 12;

            double monthlyPI = 0;
            if (monthlyRate > 0 && nPayments > 0) {
                monthlyPI = loanAmt * (monthlyRate * Math.pow(1 + monthlyRate, nPayments)) / (Math.pow(1 + monthlyRate, nPayments) - 1);
            } else if (nPayments > 0) {
                monthlyPI = loanAmt / nPayments;
            }

            double totalInterest = (monthlyPI * nPayments) - loanAmt;
            if (totalInterest < 0) totalInterest = 0;

            double monthlyTax = (price * 0.012) / 12.0;
            double monthlyIns = (price * 0.004) / 12.0;
            double totalMonthly = monthlyPI + monthlyTax + monthlyIns;

            double monthlyIncome = income / 12.0;
            double dti = (monthlyIncome > 0) ? (totalMonthly / monthlyIncome) * 100.0 : 0;

            NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);
            currency.setMaximumFractionDigits(0);

            setText(view, R.id.tvCalcMonthlyPayment, currency.format(totalMonthly) + "/mo");
            setText(view, R.id.tvCalcPI, currency.format(monthlyPI) + "/mo");
            setText(view, R.id.tvCalcTax, currency.format(monthlyTax) + "/mo");
            setText(view, R.id.tvCalcIns, currency.format(monthlyIns) + "/mo");
            
            setText(view, R.id.tvCalcDownAmt, currency.format(downAmt));
            setText(view, R.id.tvCalcLoanAmt, currency.format(loanAmt));
            setText(view, R.id.tvCalcTotalInterest, currency.format(totalInterest));
            setText(view, R.id.tvCalcDti, String.format(Locale.US, "%.1f%%", dti));

            TextView tvAffordability = view.findViewById(R.id.tvCalcAffordability);
            if (dti <= 28) {
                tvAffordability.setText("Affordability: Comfortable");
                tvAffordability.setTextColor(0xFF3DB8A8);
                tvAffordability.setBackgroundColor(0xFFE0F2F1);
            } else if (dti <= 43) {
                tvAffordability.setText("Affordability: Stretched (Edge of approval)");
                tvAffordability.setTextColor(0xFFF57C00);
                tvAffordability.setBackgroundColor(0xFFFFF3E0);
            } else {
                tvAffordability.setText("Affordability: Unlikely to be approved");
                tvAffordability.setTextColor(0xFFD32F2F);
                tvAffordability.setBackgroundColor(0xFFFFEBEE);
            }

        } catch (Exception ignored) {
        }
    }

    private void setText(View root, int id, String text) {
        TextView tv = root.findViewById(id);
        if (tv != null && text != null) tv.setText(text);
    }

    @Override public int getTheme() { return R.style.BottomSheetDialogTheme; }
}
