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
import com.example.azureestate.models.Property;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class FavoritesFragment extends Fragment {

    private RecyclerView rvFavorites;
    private LinearLayout llEmptyState;
    private TextView tvFavoriteCount;
    private Button btnBrowseProperties;
    private PropertyAdapter adapter;
    private FavoritesManager favoritesManager;
    private List<Property> allProperties;
    private List<Property> favoriteProperties;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        initViews(view);
        loadAllProperties();
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
        favoriteProperties = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new PropertyAdapter(requireContext(), favoriteProperties,
                new PropertyAdapter.OnPropertyClickListener() {
                    @Override
                    public void onPropertyClick(Property property) {
                        Toast.makeText(getContext(), property.getTitle(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFavoriteClick(Property property, int position) {
                        // Remove from favorites
                        favoritesManager.removeFavorite(property.getId());
                        property.setFavorited(false);
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

    private void loadAllProperties() {
        allProperties = new ArrayList<>();

        allProperties.add(new Property(
                1,
                "The Glass Pavilion",
                "888 Skyline Drive, Los Angeles, CA",
                "$3,450,000",
                4.9,
                4, 3, "3,200",
                2,
                "NEW LISTING",
                "APARTMENTS",
                R.drawable.property_1,
                "Defined by its seamless integration with the surrounding canyon landscape...",
                "Elena Rodriguez",
                "Senior Listing Partner",
                "This estate offers an unparalleled level of privacy and sophistication.",
                Arrays.asList("Fiber Optic WiFi", "Infinity Pool"),
                "EXCLUSIVE LISTING"
        ));

        allProperties.add(new Property(
                2,
                "Desert Monolith",
                "42 Echo Valley, Austin, TX",
                "$1,890,000",
                4.8,
                3, 2, "2,450",
                2,
                "",
                "HOUSES",
                R.drawable.property_2,
                "Rising from the high desert like a sculptural sentinel...",
                "Marcus Stone",
                "Luxury Property Advisor",
                "Desert Monolith is unlike anything else on the market in Austin.",
                Arrays.asList("Heated Floors", "Rooftop Terrace"),
                "CURATED SELECTION"
        ));

        allProperties.add(new Property(
                3,
                "Heritage Manor",
                "15 Heritage Court, Greenwich, CT",
                "$5,200,000",
                5.0,
                6, 5, "6,100",
                4,
                "RARE FIND",
                "HOUSES",
                R.drawable.property_3,
                "Steeped in the quiet grandeur of Greenwich's gold coast...",
                "Victoria Ashford",
                "Estate Portfolio Director",
                "Heritage Manor is the rarest of opportunities.",
                Arrays.asList("English Gardens", "Library & Study"),
                "RARE FIND"
        ));
    }

    private void loadFavorites() {
        favoriteProperties.clear();
        Set<Integer> favoriteIds = favoritesManager.getAllFavoriteIds();

        for (Property property : allProperties) {
            if (favoriteIds.contains(property.getId())) {
                property.setFavorited(true);
                favoriteProperties.add(property);
            }
        }

        updateUI();
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