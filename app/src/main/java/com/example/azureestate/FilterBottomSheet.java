package com.example.azureestate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.slider.RangeSlider;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FilterBottomSheet extends BottomSheetDialogFragment {

    public interface FilterListener {
        void onFiltersApplied(FilterConfig config);
    }

    // ── Filter config data class ──
    public static class FilterConfig {
        public String category    = "All Properties";
        public float  priceMin    = 100_000f;
        public float  priceMax    = 20_000_000f;
        public float  areaMin     = 500f;
        public float  areaMax     = 15_000f;
        public int    minBedrooms = 0;      // 0 = any
        public String sortBy      = "Recommended";
        public List<String> facilities = new ArrayList<>();

        public boolean isDefault() {
            return category.equals("All Properties")
                    && priceMin == 100_000f && priceMax == 20_000_000f
                    && areaMin == 500f && areaMax == 15_000f
                    && minBedrooms == 0
                    && sortBy.equals("Recommended")
                    && facilities.isEmpty();
        }
    }

    private FilterListener listener;
    private FilterConfig currentConfig;

    // Category chips
    private TextView chipAll, chipVilla, chipApartment, chipHouse, chipCondo;
    // Sort chips
    private TextView sortNewest, sortRecommended, sortHighPrice, sortLowPrice, sortRating;
    // Bedroom chips
    private TextView bedAny, bed1, bed2, bed3, bed4;
    // Facility chips
    private TextView facPool, facGym, facGarage, facSecurity, facGarden, facCinema;
    // Sliders
    private RangeSlider sliderPrice, sliderArea;
    private TextView tvPriceRange, tvAreaRange;
    // Buttons
    private Button btnApplyFilter, btnResetFilter;
    private TextView tvResetAll;

    public static FilterBottomSheet newInstance(FilterConfig config, FilterListener listener) {
        FilterBottomSheet sheet = new FilterBottomSheet();
        sheet.currentConfig = config != null ? config : new FilterConfig();
        sheet.listener = listener;
        return sheet;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        expandSheet(view);
        bindViews(view);
        restoreState();
        setupListeners();
    }

    private void expandSheet(View view) {
        View parent = (View) view.getParent();
        if (parent != null) {
            BottomSheetBehavior<View> b = BottomSheetBehavior.from(parent);
            b.setState(BottomSheetBehavior.STATE_EXPANDED);
            b.setSkipCollapsed(true);
        }
    }

    private void bindViews(View v) {
        chipAll        = v.findViewById(R.id.chipAll);
        chipVilla      = v.findViewById(R.id.chipVilla);
        chipApartment  = v.findViewById(R.id.chipApartment);
        chipHouse      = v.findViewById(R.id.chipHouse);
        chipCondo      = v.findViewById(R.id.chipCondo);

        sortNewest      = v.findViewById(R.id.sortNewest);
        sortRecommended = v.findViewById(R.id.sortRecommended);
        sortHighPrice   = v.findViewById(R.id.sortHighPrice);
        sortLowPrice    = v.findViewById(R.id.sortLowPrice);
        sortRating      = v.findViewById(R.id.sortRating);

        bedAny = v.findViewById(R.id.bedAny);
        bed1   = v.findViewById(R.id.bed1);
        bed2   = v.findViewById(R.id.bed2);
        bed3   = v.findViewById(R.id.bed3);
        bed4   = v.findViewById(R.id.bed4);

        facPool     = v.findViewById(R.id.facPool);
        facGym      = v.findViewById(R.id.facGym);
        facGarage   = v.findViewById(R.id.facGarage);
        facSecurity = v.findViewById(R.id.facSecurity);
        facGarden   = v.findViewById(R.id.facGarden);
        facCinema   = v.findViewById(R.id.facCinema);

        sliderPrice  = v.findViewById(R.id.sliderPrice);
        sliderArea   = v.findViewById(R.id.sliderArea);
        tvPriceRange = v.findViewById(R.id.tvPriceRange);
        tvAreaRange  = v.findViewById(R.id.tvAreaRange);

        btnApplyFilter = v.findViewById(R.id.btnApplyFilter);
        btnResetFilter = v.findViewById(R.id.btnResetFilter);
        tvResetAll     = v.findViewById(R.id.tvResetAll);
    }

    private void restoreState() {
        // Category
        setActiveCategory(currentConfig.category);
        // Sort
        setActiveSort(currentConfig.sortBy);
        // Bedrooms
        setActiveBed(currentConfig.minBedrooms);
        // Price slider
        sliderPrice.setValues(currentConfig.priceMin, currentConfig.priceMax);
        updatePriceLabel(currentConfig.priceMin, currentConfig.priceMax);
        // Area slider
        sliderArea.setValues(currentConfig.areaMin, currentConfig.areaMax);
        updateAreaLabel(currentConfig.areaMin, currentConfig.areaMax);
        // Facilities
        for (String fac : currentConfig.facilities) {
            TextView chip = getFacChip(fac);
            if (chip != null) setChipActive(chip, true);
        }
    }

    private void setupListeners() {
        // Category chips
        View.OnClickListener catClick = v -> {
            String cat = ((TextView) v).getText().toString();
            setActiveCategory(cat);
            currentConfig.category = cat;
        };
        chipAll.setOnClickListener(catClick);
        chipVilla.setOnClickListener(catClick);
        chipApartment.setOnClickListener(catClick);
        chipHouse.setOnClickListener(catClick);
        chipCondo.setOnClickListener(catClick);

        // Sort chips
        View.OnClickListener sortClick = v -> {
            String sort = ((TextView) v).getText().toString();
            setActiveSort(sort);
            currentConfig.sortBy = sort;
        };
        sortNewest.setOnClickListener(sortClick);
        sortRecommended.setOnClickListener(sortClick);
        sortHighPrice.setOnClickListener(sortClick);
        sortLowPrice.setOnClickListener(sortClick);
        sortRating.setOnClickListener(sortClick);

        // Bedroom chips
        bedAny.setOnClickListener(v -> { setActiveBed(0); currentConfig.minBedrooms = 0; });
        bed1.setOnClickListener(v   -> { setActiveBed(1); currentConfig.minBedrooms = 1; });
        bed2.setOnClickListener(v   -> { setActiveBed(2); currentConfig.minBedrooms = 2; });
        bed3.setOnClickListener(v   -> { setActiveBed(3); currentConfig.minBedrooms = 3; });
        bed4.setOnClickListener(v   -> { setActiveBed(4); currentConfig.minBedrooms = 4; });

        // Facility chips (toggle)
        View.OnClickListener facClick = v -> {
            TextView chip = (TextView) v;
            String fac = chip.getText().toString();
            boolean active = currentConfig.facilities.contains(fac);
            if (active) {
                currentConfig.facilities.remove(fac);
                setChipActive(chip, false);
            } else {
                currentConfig.facilities.add(fac);
                setChipActive(chip, true);
            }
            animateChip(chip);
        };
        facPool.setOnClickListener(facClick);
        facGym.setOnClickListener(facClick);
        facGarage.setOnClickListener(facClick);
        facSecurity.setOnClickListener(facClick);
        facGarden.setOnClickListener(facClick);
        facCinema.setOnClickListener(facClick);

        // Price slider
        sliderPrice.addOnChangeListener((slider, value, fromUser) -> {
            float min = slider.getValues().get(0);
            float max = slider.getValues().get(1);
            currentConfig.priceMin = min;
            currentConfig.priceMax = max;
            updatePriceLabel(min, max);
        });

        // Area slider
        sliderArea.addOnChangeListener((slider, value, fromUser) -> {
            float min = slider.getValues().get(0);
            float max = slider.getValues().get(1);
            currentConfig.areaMin = min;
            currentConfig.areaMax = max;
            updateAreaLabel(min, max);
        });

        // Apply
        btnApplyFilter.setOnClickListener(v -> {
            if (listener != null) listener.onFiltersApplied(currentConfig);
            dismiss();
        });

        // Reset
        View.OnClickListener resetClick = v -> {
            currentConfig = new FilterConfig();
            restoreState();
        };
        btnResetFilter.setOnClickListener(resetClick);
        tvResetAll.setOnClickListener(resetClick);
    }

    // ── Chip state helpers ──
    private void setActiveCategory(String cat) {
        TextView[] chips = {chipAll, chipVilla, chipApartment, chipHouse, chipCondo};
        for (TextView c : chips) {
            boolean active = c.getText().toString().equals(cat);
            c.setBackgroundResource(active ? R.drawable.chip_filter_active : R.drawable.chip_inactive);
            c.setTextColor(active
                    ? ContextCompat.getColor(requireContext(), android.R.color.white)
                    : 0xFF7A8899);
        }
    }

    private void setActiveSort(String sort) {
        TextView[] chips = {sortNewest, sortRecommended, sortHighPrice, sortLowPrice, sortRating};
        for (TextView c : chips) {
            boolean active = c.getText().toString().equals(sort);
            c.setBackgroundResource(active ? R.drawable.chip_filter_active : R.drawable.chip_inactive);
            c.setTextColor(active
                    ? ContextCompat.getColor(requireContext(), android.R.color.white)
                    : 0xFF7A8899);
        }
    }

    private void setActiveBed(int min) {
        TextView[] chips = {bedAny, bed1, bed2, bed3, bed4};
        int[] values     = {0, 1, 2, 3, 4};
        for (int i = 0; i < chips.length; i++) {
            boolean active = values[i] == min;
            chips[i].setBackgroundResource(active ? R.drawable.chip_filter_active : R.drawable.chip_inactive);
            chips[i].setTextColor(active
                    ? ContextCompat.getColor(requireContext(), android.R.color.white)
                    : 0xFF7A8899);
        }
    }

    private void setChipActive(TextView chip, boolean active) {
        chip.setBackgroundResource(active ? R.drawable.chip_filter_active : R.drawable.chip_inactive);
        chip.setTextColor(active
                ? ContextCompat.getColor(requireContext(), android.R.color.white)
                : 0xFF7A8899);
    }

    private void animateChip(View v) {
        v.animate().scaleX(1.08f).scaleY(1.08f).setDuration(90)
                .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(90).start())
                .start();
    }

    private TextView getFacChip(String name) {
        switch (name) {
            case "Pool":     return facPool;
            case "Gym":      return facGym;
            case "Garage":   return facGarage;
            case "Security": return facSecurity;
            case "Garden":   return facGarden;
            case "Cinema":   return facCinema;
            default:         return null;
        }
    }

    private void updatePriceLabel(float min, float max) {
        tvPriceRange.setText(formatPrice(min) + " – " + formatPrice(max));
    }

    private void updateAreaLabel(float min, float max) {
        tvAreaRange.setText(String.format(Locale.US, "%.0f – %.0f sqft", min, max));
    }

    private String formatPrice(float value) {
        if (value >= 1_000_000)
            return String.format(Locale.US, "$%.1fM", value / 1_000_000).replace(".0M", "M");
        return String.format(Locale.US, "$%.0fK", value / 1_000);
    }

    @Override public int getTheme() { return R.style.BottomSheetDialogTheme; }
}