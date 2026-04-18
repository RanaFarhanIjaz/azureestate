package com.example.azureestate;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.HorizontalScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.azureestate.models.Property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SearchFragment extends Fragment
        implements FilterBottomSheet.FilterListener {

    // Views
    private EditText etSearch;
    private CardView btnFilter;
    private View filterActiveBadge;
    private TextView tvResultCount, tvCurrentSort;
    private HorizontalScrollView hsvActiveFilters;
    private LinearLayout llActiveFilters;
    private RecyclerView rvSearchResults;
    private LinearLayout llEmptyState, llLoadingState;

    // Data
    private List<Property> allProperties   = new ArrayList<>();
    private List<Property> filteredList    = new ArrayList<>();
    private PropertyAdapter adapter;
    private FilterBottomSheet.FilterConfig currentFilter = new FilterBottomSheet.FilterConfig();

    // Debounce search
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final int SEARCH_DEBOUNCE_MS = 350;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        loadAllProperties();
        setupAdapter();
        setupSearch();
        setupFilterButton();
        // Initial display: show all
        applyFiltersAndSearch("");
    }

    // ─────────────────────────────────────────────────────────────
    private void bindViews(View v) {
        etSearch          = v.findViewById(R.id.etSearch);
        btnFilter         = v.findViewById(R.id.btnFilter);
        filterActiveBadge = v.findViewById(R.id.filterActiveBadge);
        tvResultCount     = v.findViewById(R.id.tvResultCount);
        tvCurrentSort     = v.findViewById(R.id.tvCurrentSort);
        hsvActiveFilters  = v.findViewById(R.id.hsvActiveFilters);
        llActiveFilters   = v.findViewById(R.id.llActiveFilters);
        rvSearchResults   = v.findViewById(R.id.rvSearchResults);
        llEmptyState      = v.findViewById(R.id.llEmptyState);
        llLoadingState    = v.findViewById(R.id.llLoadingState);
    }

    // ─────────────────────────────────────────────────────────────
    //  Load full property dataset (same as HomeFragment + more)
    // ─────────────────────────────────────────────────────────────
    private void loadAllProperties() {
        allProperties.clear();
        allProperties.add(new Property(1, "The Glass Pavilion",
                "888 Skyline Drive, Los Angeles, CA", "$3,450,000", 4.9,
                4, 3, "3,200", 2, "NEW LISTING", "Apartment",
                R.drawable.property_1,
                "A modern architectural masterpiece with floor-to-ceiling glass and canyon views.",
                "Elena Rodriguez", "Senior Listing Partner",
                "An unparalleled level of privacy and sophistication.",
                Arrays.asList("Pool", "Gym", "Cinema", "Security"), "EXCLUSIVE LISTING"));

        allProperties.add(new Property(2, "Desert Monolith",
                "42 Echo Valley, Austin, TX", "$1,890,000", 4.8,
                3, 2, "2,450", 2, "", "House",
                R.drawable.property_2,
                "Brutalist desert home rising from the Texas landscape.",
                "Marcus Stone", "Luxury Property Advisor",
                "Desert Monolith is unlike anything else on the market.",
                Arrays.asList("Garage", "Garden"), "CURATED SELECTION"));

        allProperties.add(new Property(3, "Heritage Manor",
                "15 Heritage Court, Greenwich, CT", "$5,200,000", 5.0,
                6, 5, "6,100", 4, "RARE FIND", "House",
                R.drawable.property_3,
                "Restored 1928 estate on 2.4 private acres with English gardens.",
                "Victoria Ashford", "Estate Portfolio Director",
                "A fully restored landmark with every modern comfort.",
                Arrays.asList("Garden", "Security", "Gym"), "RARE FIND"));

        allProperties.add(new Property(4, "Skyline Penthouse",
                "One Manhattan West, New York, NY", "$8,750,000", 4.9,
                4, 4, "4,800", 0, "", "Apartment",
                R.drawable.property_1,
                "Full-floor penthouse with 360° views of the Hudson River.",
                "James Whitfield", "Manhattan Luxury Division",
                "The pinnacle of New York living.",
                Arrays.asList("Pool", "Security", "Gym", "Cinema"), "EXCLUSIVE LISTING"));

        allProperties.add(new Property(5, "Amalfi Villa",
                "Sunset Blvd, Beverly Hills, CA", "$6,900,000", 4.7,
                5, 4, "5,500", 3, "", "Villa",
                R.drawable.property_2,
                "Mediterranean-inspired villa with panoramic hillside views.",
                "Sofia Marchetti", "Villa Specialist",
                "A slice of the Mediterranean in the heart of Beverly Hills.",
                Arrays.asList("Pool", "Garden", "Security", "Garage"), "EXCLUSIVE LISTING"));

        allProperties.add(new Property(6, "Harbor Condo",
                "200 Marina Blvd, Miami, FL", "$980,000", 4.5,
                2, 2, "1,800", 1, "", "Condo",
                R.drawable.property_3,
                "Modern condo with direct marina views and resort amenities.",
                "Carlos Vega", "Miami Waterfront Specialist",
                "Waking up to the marina every morning never gets old.",
                Arrays.asList("Pool", "Gym", "Security"), ""));

        allProperties.add(new Property(7, "Oak Ridge Estate",
                "12 Magnolia Lane, Nashville, TN", "$2,100,000", 4.6,
                5, 3, "4,200", 2, "", "House",
                R.drawable.property_1,
                "Classic Tennessee estate with wraparound porch and oak-lined drive.",
                "Anna Williams", "Southern Estate Advisor",
                "There is no place more welcoming than Oak Ridge.",
                Arrays.asList("Garden", "Garage", "Security"), ""));

        allProperties.add(new Property(8, "The Ivory Tower",
                "450 Park Ave, New York, NY", "$12,500,000", 5.0,
                6, 6, "7,200", 0, "RARE FIND", "Apartment",
                R.drawable.property_2,
                "Trophy apartment on Park Avenue — the pinnacle of Manhattan luxury.",
                "James Whitfield", "Manhattan Luxury Division",
                "An address that speaks for itself.",
                Arrays.asList("Pool", "Cinema", "Security", "Gym"), "EXCLUSIVE LISTING"));
    }

    // ─────────────────────────────────────────────────────────────
    private void setupAdapter() {
        adapter = new PropertyAdapter(requireContext(), new ArrayList<>(),
                new PropertyAdapter.OnPropertyClickListener() {
                    @Override
                    public void onPropertyClick(Property property) {
                        PropertyDetailBottomSheet.newInstance(property)
                                .show(getChildFragmentManager(), "Detail");
                    }
                    @Override
                    public void onFavoriteClick(Property property, int position) {
                        property.setFavorited(!property.isFavorited());
                        adapter.notifyItemChanged(position);
                    }
                });
        rvSearchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSearchResults.setAdapter(adapter);
        rvSearchResults.setNestedScrollingEnabled(true);
    }

    // ─────────────────────────────────────────────────────────────
    //  Live search with debounce
    // ─────────────────────────────────────────────────────────────
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int i, int b, int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> applyFiltersAndSearch(s.toString().trim());
                searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_MS);
            }
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
            applyFiltersAndSearch(etSearch.getText().toString().trim());
            return false;
        });
    }

    // ─────────────────────────────────────────────────────────────
    //  Filter button → open FilterBottomSheet
    // ─────────────────────────────────────────────────────────────
    private void setupFilterButton() {
        btnFilter.setOnClickListener(v -> {
            FilterBottomSheet sheet = FilterBottomSheet.newInstance(currentFilter, this);
            sheet.show(getChildFragmentManager(), "Filter");
        });
    }

    // ─────────────────────────────────────────────────────────────
    //  FilterListener callback
    // ─────────────────────────────────────────────────────────────
    @Override
    public void onFiltersApplied(FilterBottomSheet.FilterConfig config) {
        currentFilter = config;
        tvCurrentSort.setText(config.sortBy);

        // Show/hide filter active badge
        boolean active = !config.isDefault();
        filterActiveBadge.setVisibility(active ? View.VISIBLE : View.GONE);

        // Rebuild active filter chips
        buildActiveFilterChips(config);

        applyFiltersAndSearch(etSearch.getText().toString().trim());
    }

    // ─────────────────────────────────────────────────────────────
    //  Core filter + search + sort logic
    // ─────────────────────────────────────────────────────────────
    private void applyFiltersAndSearch(String query) {
        showLoading(true);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            List<Property> result = new ArrayList<>();

            for (Property p : allProperties) {
                // 1. Text search (title, address, category)
                if (!query.isEmpty()) {
                    String q = query.toLowerCase();
                    boolean match = p.getTitle().toLowerCase().contains(q)
                            || p.getAddress().toLowerCase().contains(q)
                            || p.getCategory().toLowerCase().contains(q);
                    if (!match) continue;
                }

                // 2. Category filter
                if (!currentFilter.category.equals("All Properties")) {
                    if (!p.getCategory().equalsIgnoreCase(currentFilter.category)) continue;
                }

                // 3. Price filter (parse numeric from "$X,XXX,XXX")
                float price = parsePrice(p.getPrice());
                if (price < currentFilter.priceMin || price > currentFilter.priceMax) continue;

                // 4. Area filter
                float area = parseSqft(p.getSqft());
                if (area < currentFilter.areaMin || area > currentFilter.areaMax) continue;

                // 5. Bedrooms filter
                if (currentFilter.minBedrooms > 0 && p.getBeds() < currentFilter.minBedrooms) continue;

                // 6. Facilities filter (property must have ALL selected facilities)
                if (!currentFilter.facilities.isEmpty()) {
                    List<String> amenities = p.getAmenities();
                    boolean hasAll = true;
                    for (String fac : currentFilter.facilities) {
                        if (amenities == null || !amenities.contains(fac)) {
                            hasAll = false;
                            break;
                        }
                    }
                    if (!hasAll) continue;
                }

                result.add(p);
            }

            // 7. Sort
            sortList(result, currentFilter.sortBy);

            filteredList = result;
            showLoading(false);
            updateUI(filteredList);
        }, 300); // slight delay for loading UX
    }

    private void sortList(List<Property> list, String sortBy) {
        switch (sortBy) {
            case "Highest Price":
                Collections.sort(list, (a, b) ->
                        Float.compare(parsePrice(b.getPrice()), parsePrice(a.getPrice())));
                break;
            case "Lowest Price":
                Collections.sort(list, (a, b) ->
                        Float.compare(parsePrice(a.getPrice()), parsePrice(b.getPrice())));
                break;
            case "Top Rated":
                Collections.sort(list, (a, b) ->
                        Double.compare(b.getRating(), a.getRating()));
                break;
            case "Newest":
                // Reverse original order (simulate newest first)
                Collections.reverse(list);
                break;
            default: // Recommended — keep original order
                break;
        }
    }

    private void updateUI(List<Property> list) {
        if (!isAdded()) return;

        if (list.isEmpty()) {
            rvSearchResults.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
            tvResultCount.setText("No properties found");
        } else {
            rvSearchResults.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
            tvResultCount.setText(list.size() + " " +
                    (list.size() == 1 ? "property" : "properties") + " found");
        }

        adapter.updateList(list);

        // Fade in
        rvSearchResults.setAlpha(0f);
        rvSearchResults.animate().alpha(1f).setDuration(250).start();
    }

    private void showLoading(boolean show) {
        if (!isAdded()) return;
        llLoadingState.setVisibility(show ? View.VISIBLE : View.GONE);
        rvSearchResults.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        llEmptyState.setVisibility(View.GONE);
    }

    // ─────────────────────────────────────────────────────────────
    //  Active filter chips row
    // ─────────────────────────────────────────────────────────────
    private void buildActiveFilterChips(FilterBottomSheet.FilterConfig config) {
        llActiveFilters.removeAllViews();
        List<String> labels = new ArrayList<>();

        if (!config.category.equals("All Properties")) labels.add(config.category);
        if (!config.sortBy.equals("Recommended"))       labels.add("Sort: " + config.sortBy);
        if (config.minBedrooms > 0)                     labels.add(config.minBedrooms + "+ beds");
        labels.addAll(config.facilities);

        if (labels.isEmpty()) {
            hsvActiveFilters.setVisibility(View.GONE);
            return;
        }
        hsvActiveFilters.setVisibility(View.VISIBLE);

        for (String label : labels) {
            TextView chip = new TextView(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(8);
            chip.setLayoutParams(lp);
            chip.setText(label + " ×");
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.teal_primary));
            chip.setTextSize(11f);
            chip.setBackgroundResource(R.drawable.chip_active);
            chip.setPadding(24, 12, 24, 12);
            chip.setOnClickListener(v -> {
                // Remove this filter
                removeFilter(label, config);
                buildActiveFilterChips(config);
                applyFiltersAndSearch(etSearch.getText().toString().trim());
            });
            llActiveFilters.addView(chip);
        }
    }

    private void removeFilter(String label, FilterBottomSheet.FilterConfig config) {
        if (label.contains("Sort:"))         config.sortBy = "Recommended";
        else if (label.contains("beds"))     config.minBedrooms = 0;
        else if (config.facilities.contains(label.replace(" ×", "")))
            config.facilities.remove(label.replace(" ×", ""));
        else                                 config.category = "All Properties";
    }

    // ─────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────
    private float parsePrice(String price) {
        try { return Float.parseFloat(price.replaceAll("[^0-9.]", "")); }
        catch (Exception e) { return 0; }
    }

    private float parseSqft(String sqft) {
        try { return Float.parseFloat(sqft.replaceAll("[^0-9.]", "")); }
        catch (Exception e) { return 0; }
    }
}