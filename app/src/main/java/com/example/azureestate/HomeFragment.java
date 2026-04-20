package com.example.azureestate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment
        implements SupabaseManager.RealtimeListener {

    private RecyclerView rvProperties;
    private LinearLayout llLoadingState, llEmptyState;
    private LinearLayout catHouses, catApartments, catCondos, catVillas;
    private String selectedCategory = "ALL";

    private final List<SupabaseManager.ListingData> allListings      = new ArrayList<>();
    private final List<SupabaseManager.ListingData> filteredListings = new ArrayList<>();
    private SupabasePropAdapter adapter;
    private SupabaseManager supabase;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        supabase = SupabaseManager.getInstance(requireContext());
        bindViews(view);
        setupCategoryFilters();
        setupAdapter();
        loadListings();
        supabase.subscribeToListings(this);
    }

    private void bindViews(View v) {
        rvProperties   = v.findViewById(R.id.rvProperties);
        llLoadingState = v.findViewById(R.id.llLoadingState);
        llEmptyState   = v.findViewById(R.id.llEmptyState);
        catHouses      = v.findViewById(R.id.catHouses);
        catApartments  = v.findViewById(R.id.catApartments);
        catCondos      = v.findViewById(R.id.catCondos);
        catVillas      = v.findViewById(R.id.catVillas);

        android.widget.ImageView ivMessages = v.findViewById(R.id.ivMessages);
        if (ivMessages != null) {
            ivMessages.setOnClickListener(vw -> {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .add(R.id.fragmentContainer, new MessagesFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        TextView tvViewAll = v.findViewById(R.id.tvViewAll);
        if (tvViewAll != null) tvViewAll.setOnClickListener(vw -> {
            selectedCategory = "ALL";
            resetCategoryUI();
            applyCategory();
        });
    }

    private void setupCategoryFilters() {
        if (catHouses     != null) catHouses.setOnClickListener(v     -> setCategory("House",     catHouses));
        if (catApartments != null) catApartments.setOnClickListener(v -> setCategory("Apartment", catApartments));
        if (catCondos     != null) catCondos.setOnClickListener(v     -> setCategory("Condo",     catCondos));
        if (catVillas     != null) catVillas.setOnClickListener(v     -> setCategory("Villa",     catVillas));
    }

    private void setCategory(String cat, LinearLayout selected) {
        selectedCategory = cat;
        resetCategoryUI();
        selected.setBackgroundResource(R.drawable.category_bg_active);
        selected.animate().scaleX(1.06f).scaleY(1.06f).setDuration(100)
                .withEndAction(() -> selected.animate().scaleX(1f).scaleY(1f).setDuration(100).start()).start();
        applyCategory();
    }

    private void resetCategoryUI() {
        if (catHouses     != null) catHouses.setBackgroundResource(R.drawable.category_bg_inactive);
        if (catApartments != null) catApartments.setBackgroundResource(R.drawable.category_bg_inactive);
        if (catCondos     != null) catCondos.setBackgroundResource(R.drawable.category_bg_inactive);
        if (catVillas     != null) catVillas.setBackgroundResource(R.drawable.category_bg_inactive);
    }

    private void applyCategory() {
        filteredListings.clear();
        for (SupabaseManager.ListingData d : allListings) {
            if (selectedCategory.equals("ALL")) {
                filteredListings.add(d);
            } else if (d.category != null) {
                String filterCat = selectedCategory.toLowerCase();
                String propCat = d.category.toLowerCase();
                // Allow "House" to match "HOUSES" and vice-versa
                if (propCat.contains(filterCat) || filterCat.contains(propCat)) {
                    filteredListings.add(d);
                }
            }
        }
        adapter.updateList(filteredListings);
        updateEmptyState();
    }

    private void setupAdapter() {
        adapter = new SupabasePropAdapter(requireContext(), filteredListings,
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
        rvProperties.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvProperties.setAdapter(adapter);
        rvProperties.setNestedScrollingEnabled(false);
    }

    private void loadListings() {
        showLoading(true);
        SupabaseManager.FetchParams params = new SupabaseManager.FetchParams();
        params.limit = 30;

        supabase.fetchListings(params, new SupabaseManager.ListingsCallback() {
            @Override
            public void onSuccess(List<SupabaseManager.ListingData> listings) {
                if (!isAdded()) return;
                showLoading(false);
                allListings.clear();
                allListings.addAll(listings);
                applyCategory();
                rvProperties.setAlpha(0f);
                rvProperties.animate().alpha(1f).setDuration(350).start();
            }
            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                showLoading(false);
                updateEmptyState();
            }
        });
    }

    // ── Realtime callbacks ────────────────────────────────────
    @Override
    public void onNewListing(SupabaseManager.ListingData listing) {
        if (!isAdded()) return;
        allListings.add(0, listing);
        if (selectedCategory.equals("ALL") || listing.category.equalsIgnoreCase(selectedCategory)) {
            filteredListings.add(0, listing);
            adapter.notifyItemInserted(0);
            rvProperties.smoothScrollToPosition(0);
            if (getView() != null) {
                Snackbar.make(getView(), "✦ New listing: " + listing.title, 3000)
                        .setBackgroundTint(0xFF3DB8A8).setTextColor(0xFFFFFFFF).show();
            }
        }
        updateEmptyState();
    }

    @Override
    public void onListingUpdated(SupabaseManager.ListingData updated) {
        if (!isAdded()) return;
        for (int i = 0; i < filteredListings.size(); i++) {
            if (filteredListings.get(i).id.equals(updated.id)) {
                filteredListings.set(i, updated);
                adapter.notifyItemChanged(i);
                break;
            }
        }
    }

    private void showLoading(boolean show) {
        if (!isAdded()) return;
        if (llLoadingState != null) llLoadingState.setVisibility(show ? View.VISIBLE : View.GONE);
        if (rvProperties   != null) rvProperties.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
    }

    private void updateEmptyState() {
        if (!isAdded() || llEmptyState == null) return;
        boolean empty = filteredListings.isEmpty();
        llEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvProperties.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}