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

import java.util.ArrayList;
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
    private List<SupabaseManager.ListingData> allProperties   = new ArrayList<>();
    private List<SupabaseManager.ListingData> filteredList    = new ArrayList<>();
    private SupabasePropAdapter adapter;
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
    //  Load full property dataset from Supabase
    // ─────────────────────────────────────────────────────────────
    private void loadAllProperties() {
        showLoading(true);
        SupabaseManager.FetchParams params = new SupabaseManager.FetchParams();
        params.limit = 100;
        
        SupabaseManager.getInstance(requireContext()).fetchListings(params, new SupabaseManager.ListingsCallback() {
            @Override
            public void onSuccess(List<SupabaseManager.ListingData> listings) {
                if (!isAdded()) return;
                allProperties.clear();
                allProperties.addAll(listings);
                applyFiltersAndSearch(etSearch.getText().toString().trim());
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                showLoading(false);
            }
        });
    }

    // ─────────────────────────────────────────────────────────────
    private void setupAdapter() {
        adapter = new SupabasePropAdapter(requireContext(), new ArrayList<>(),
                new SupabasePropAdapter.OnClickListener() {
                    @Override
                    public void onClick(SupabaseManager.ListingData property) {
                        SupabaseDetailBottomSheet.newInstance(property)
                                .show(getChildFragmentManager(), "Detail");
                    }
                    @Override
                    public void onFavoriteClick(SupabaseManager.ListingData property, int position) {
                        FavoritesManager.getInstance(requireContext()).toggleFavorite(property.id);
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
            List<SupabaseManager.ListingData> result = new ArrayList<>();

            for (SupabaseManager.ListingData p : allProperties) {
                // 1. Enhanced Text search (title, address, category, description, badge)
                if (!query.isEmpty()) {
                    String q = query.toLowerCase();
                    String searchableText = ((p.title != null ? p.title : "") + " "
                            + (p.address != null ? p.address : "") + " "
                            + (p.category != null ? p.category : "") + " "
                            + (p.description != null ? p.description : "") + " "
                            + (p.badge != null ? p.badge : "")).toLowerCase();
                    
                    // Split query into individual words so "villa beverly" matches "Villa in Beverly Hills"
                    String[] terms = q.split("\\s+");
                    boolean matchAll = true;
                    for (String term : terms) {
                        if (!searchableText.contains(term)) {
                            matchAll = false;
                            break;
                        }
                    }
                    if (!matchAll) continue;
                }

                // 2. Category filter
                if (!currentFilter.category.equals("All Properties")) {
                    if (p.category == null) continue;
                    String filterCat = currentFilter.category.toLowerCase();
                    String propCat = p.category.toLowerCase();
                    // Allow "House" to match "HOUSES" and vice-versa
                    if (!propCat.contains(filterCat) && !filterCat.contains(propCat)) continue;
                }

                // 3. Price filter (parse numeric from "$X,XXX,XXX")
                float price = parsePrice(p.price);
                if (price < currentFilter.priceMin || price > currentFilter.priceMax) continue;

                // 4. Area filter
                float area = parseSqft(p.sqft);
                if (area < currentFilter.areaMin || area > currentFilter.areaMax) continue;

                // 5. Bedrooms filter
                if (currentFilter.minBedrooms > 0 && p.beds < currentFilter.minBedrooms) continue;

                // 6. Facilities filter (check description)
                if (!currentFilter.facilities.isEmpty()) {
                    boolean hasAll = true;
                    String desc = p.description != null ? p.description.toLowerCase() : "";
                    for (String fac : currentFilter.facilities) {
                        if (!desc.contains(fac.toLowerCase())) {
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
        }, 150); // slight delay for loading UX
    }

    private void sortList(List<SupabaseManager.ListingData> list, String sortBy) {
        switch (sortBy) {
            case "Highest Price":
                Collections.sort(list, (a, b) ->
                        Float.compare(parsePrice(b.price), parsePrice(a.price)));
                break;
            case "Lowest Price":
                Collections.sort(list, (a, b) ->
                        Float.compare(parsePrice(a.price), parsePrice(b.price)));
                break;
            case "Top Rated":
                Collections.sort(list, (a, b) ->
                        Double.compare(b.rating, a.rating));
                break;
            case "Newest":
                // Original fetching was desc by created_at, so reverse isn't perfectly "newest" but helps
                Collections.reverse(list);
                break;
            default: // Recommended — keep original order
                break;
        }
    }

    private void updateUI(List<SupabaseManager.ListingData> list) {
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

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}