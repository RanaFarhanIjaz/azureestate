package com.example.azureestate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FavoritesFragment extends Fragment {

    private RecyclerView rvFavorites;
    private LinearLayout llEmptyState;
    private TextView tvFavoriteCount;
    private Button btnBrowseProperties;
    
    private SupabasePropAdapter adapter;
    private FavoritesManager favoritesManager;
    private final List<SupabaseManager.ListingData> favoriteProperties = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadFavorites();

        return view;
    }

    private void initViews(View view) {
        rvFavorites = view.findViewById(R.id.rvFavorites);
        llEmptyState = view.findViewById(R.id.llEmptyState);
        tvFavoriteCount = view.findViewById(R.id.tvFavoriteCount);
        btnBrowseProperties = view.findViewById(R.id.btnBrowseProperties);

        favoritesManager = FavoritesManager.getInstance(getContext());
    }

    private void setupRecyclerView() {
        adapter = new SupabasePropAdapter(requireContext(), favoriteProperties,
                new SupabasePropAdapter.OnClickListener() {
                    @Override
                    public void onClick(SupabaseManager.ListingData property) {
                        SupabaseDetailBottomSheet.newInstance(property)
                                .show(getChildFragmentManager(), "Detail");
                    }

                    @Override
                    public void onFavoriteClick(SupabaseManager.ListingData property, int position) {
                        // Remove from favorites
                        favoritesManager.removeFavorite(property.id);
                        favoriteProperties.remove(position);
                        adapter.notifyItemRemoved(position);
                        updateUI();
                        Toast.makeText(getContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
                    }
                });

        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFavorites.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnBrowseProperties.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToHome();
            }
        });
    }

    private void loadFavorites() {
        Set<String> favoriteIds = favoritesManager.getAllFavorites();
        if (favoriteIds.isEmpty()) {
            favoriteProperties.clear();
            updateUI();
            return;
        }

        SupabaseManager.FetchParams params = new SupabaseManager.FetchParams();
        params.limit = 100; // fetch enough to cover favorites
        
        SupabaseManager.getInstance(requireContext()).fetchListings(params, new SupabaseManager.ListingsCallback() {
            @Override
            public void onSuccess(List<SupabaseManager.ListingData> listings) {
                if (!isAdded()) return;
                favoriteProperties.clear();
                for (SupabaseManager.ListingData d : listings) {
                    if (favoriteIds.contains(d.id)) {
                        favoriteProperties.add(d);
                    }
                }
                updateUI();
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Failed to load favorites", Toast.LENGTH_SHORT).show();
                updateUI();
            }
        });
    }

    private void updateUI() {
        if (favoriteProperties.isEmpty()) {
            rvFavorites.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
            tvFavoriteCount.setText("0 saved properties");
        } else {
            rvFavorites.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
            tvFavoriteCount.setText(favoriteProperties.size() + " saved " +
                (favoriteProperties.size() == 1 ? "property" : "properties"));
            adapter.updateList(favoriteProperties);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }
}