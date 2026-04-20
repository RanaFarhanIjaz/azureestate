package com.example.azureestate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.azureestate.models.Property;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Arrays;
import java.util.List;

public class PropertyDetailBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_PROPERTY_ID = "property_id";

    // Amenity icons mapped by name (use your own drawables)
    private static final List<String> AMENITY_ICONS = Arrays.asList(
            "ic_wifi", "ic_pool", "ic_gym", "ic_cinema",
            "ic_wine", "ic_security"
    );

    private Property property;

    public static PropertyDetailBottomSheet newInstance(Property property) {
        PropertyDetailBottomSheet sheet = new PropertyDetailBottomSheet();
        Bundle args = new Bundle();
        args.putInt(ARG_PROPERTY_ID, property.getId());
        sheet.setArguments(args);
        // Pass property directly via static ref (or use Parcelable in production)
        sheet.property = property;
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_property_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (property == null) return;

        setupBottomSheetBehavior(view);
        bindViews(view);
    }

    private void setupBottomSheetBehavior(View view) {
        View parent = (View) view.getParent();
        if (parent != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(parent);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(false);
            behavior.setPeekHeight(560);
        }
    }

    private void bindViews(View view) {
        // Image
        ImageView ivDetailImage = view.findViewById(R.id.ivDetailImage);
        ivDetailImage.setImageResource(property.getImageResId());

        // Exclusive tag
        TextView tvExclusiveTag = view.findViewById(R.id.tvExclusiveTag);
        if (property.getExclusiveTag() != null && !property.getExclusiveTag().isEmpty()) {
            tvExclusiveTag.setText(property.getExclusiveTag());
            tvExclusiveTag.setVisibility(View.VISIBLE);
        } else {
            tvExclusiveTag.setVisibility(View.GONE);
        }

        // Title, address, price
        ((TextView) view.findViewById(R.id.tvDetailTitle)).setText(property.getTitle());
        ((TextView) view.findViewById(R.id.tvDetailAddress)).setText(property.getAddress());
        ((TextView) view.findViewById(R.id.tvDetailPrice)).setText(property.getPrice());

        // Stats
        ((TextView) view.findViewById(R.id.tvDetailBeds)).setText(String.valueOf(property.getBeds()));
        ((TextView) view.findViewById(R.id.tvDetailBaths)).setText(String.valueOf(property.getBaths()));
        ((TextView) view.findViewById(R.id.tvDetailSqft)).setText(property.getSqft());
        ((TextView) view.findViewById(R.id.tvDetailGarages)).setText(String.valueOf(property.getGarages()));

        // Description
        ((TextView) view.findViewById(R.id.tvDescription)).setText(property.getDescription());

        // Amenities
        buildAmenities(view);

        // Agent
        ((TextView) view.findViewById(R.id.tvAgentName)).setText(property.getAgentName());
        ((TextView) view.findViewById(R.id.tvAgentTitle)).setText(property.getAgentTitle());
        ((TextView) view.findViewById(R.id.tvAgentQuote)).setText("\"" + property.getAgentQuote() + "\"");

        // Get Directions
        view.findViewById(R.id.tvGetDirections).setOnClickListener(v -> openMaps());



        // Bottom action buttons
        view.findViewById(R.id.btnContactAgent).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Contacting " + property.getAgentName(), Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btnScheduleTour).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Tour scheduled!", Toast.LENGTH_SHORT).show());
    }

    private void buildAmenities(View view) {
        GridLayout glAmenities = view.findViewById(R.id.glAmenities);
        glAmenities.removeAllViews();

        if (property.getAmenities() == null) return;

        for (int i = 0; i < property.getAmenities().size(); i++) {
            String amenity = property.getAmenities().get(i);

            LinearLayout item = new LinearLayout(requireContext());
            item.setOrientation(LinearLayout.HORIZONTAL);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.columnSpec = GridLayout.spec(i % 2, 1f);
            params.rowSpec    = GridLayout.spec(i / 2);
            params.width      = 0;
            params.setMargins(0, 0, 0, 12);
            item.setLayoutParams(params);
            item.setGravity(android.view.Gravity.CENTER_VERTICAL);

            // Icon
            ImageView icon = new ImageView(requireContext());
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(20, 20);
            icon.setLayoutParams(iconParams);
            // Use a generic icon; swap per amenity in production
            icon.setImageResource(R.drawable.ic_amenity_check);
            icon.setColorFilter(requireContext().getColor(R.color.teal_primary));

            // Text
            TextView tv = new TextView(requireContext());
            tv.setText(amenity);
            tv.setTextColor(requireContext().getColor(R.color.text_secondary));
            tv.setTextSize(12f);
            tv.setPadding(10, 0, 0, 0);

            item.addView(icon);
            item.addView(tv);
            glAmenities.addView(item);
        }
    }

    private void openMaps() {
        String query = Uri.encode(property.getAddress());
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + query);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Uri webUri = Uri.parse("https://maps.google.com/?q=" + query);
            startActivity(new Intent(Intent.ACTION_VIEW, webUri));
        }
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }
}