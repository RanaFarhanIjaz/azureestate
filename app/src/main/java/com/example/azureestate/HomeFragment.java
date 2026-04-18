package com.example.azureestate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.azureestate.models.Property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvProperties;
    private PropertyAdapter adapter;
    private List<Property> propertyList;
    private FavoritesManager favoritesManager;

    // Category views
    private LinearLayout catHouses, catApartments, catCondos, catVillas;
    private String selectedCategory = "APARTMENTS";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        favoritesManager = FavoritesManager.getInstance(requireContext());

        bindViews(view);
        setupCategoryFilters();
        loadProperties();
        loadFavoriteStates();
        setupRecyclerView();
    }

    private void bindViews(View view) {
        rvProperties   = view.findViewById(R.id.rvProperties);
        catHouses      = view.findViewById(R.id.catHouses);
        catApartments  = view.findViewById(R.id.catApartments);
        catCondos      = view.findViewById(R.id.catCondos);
        catVillas      = view.findViewById(R.id.catVillas);

        // Filter icon
        view.findViewById(R.id.ivFilter).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Filters coming soon", Toast.LENGTH_SHORT).show());

        // View all
        view.findViewById(R.id.tvViewAll).setOnClickListener(v ->
                Toast.makeText(requireContext(), "All categories", Toast.LENGTH_SHORT).show());
    }

    private void setupCategoryFilters() {
        catHouses.setOnClickListener(v     -> setCategory("HOUSES",     catHouses));
        catApartments.setOnClickListener(v -> setCategory("APARTMENTS", catApartments));
        catCondos.setOnClickListener(v     -> setCategory("CONDOS",     catCondos));
        catVillas.setOnClickListener(v     -> setCategory("VILLAS",     catVillas));
    }

    private void setCategory(String category, LinearLayout selected) {
        selectedCategory = category;

        // Reset all to inactive
        catHouses.setBackground(requireContext().getDrawable(R.drawable.category_bg_inactive));
        catApartments.setBackground(requireContext().getDrawable(R.drawable.category_bg_inactive));
        catCondos.setBackground(requireContext().getDrawable(R.drawable.category_bg_inactive));
        catVillas.setBackground(requireContext().getDrawable(R.drawable.category_bg_inactive));

        // Set selected active
        selected.setBackground(requireContext().getDrawable(R.drawable.category_bg_active));

        // Animate selection
        selected.animate().scaleX(1.05f).scaleY(1.05f).setDuration(100)
                .withEndAction(() -> selected.animate().scaleX(1f).scaleY(1f).setDuration(100).start())
                .start();

        filterProperties();
    }

    private void filterProperties() {
        List<Property> filtered = new ArrayList<>();
        for (Property p : propertyList) {
            if (p.getCategory().equalsIgnoreCase(selectedCategory) ||
                    selectedCategory.equals("ALL")) {
                filtered.add(p);
            }
        }
        // Show all if nothing matches
        adapter.updateList(filtered.isEmpty() ? propertyList : filtered);
    }

    private void loadFavoriteStates() {
        for (Property property : propertyList) {
            property.setFavorited(favoritesManager.isFavorite(property.getId()));
        }
    }

    private void loadProperties() {
        propertyList = new ArrayList<>();

        propertyList.add(new Property(
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
                Arrays.asList("Fiber Optic WiFi", "Infinity Pool", "Private Gym", "Cinema Room"),
                "EXCLUSIVE LISTING"
        ));

        propertyList.add(new Property(
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
                Arrays.asList("Heated Floors", "Rooftop Terrace", "Home Office", "Smart Home"),
                "CURATED SELECTION"
        ));

        propertyList.add(new Property(
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
                Arrays.asList("English Gardens", "Library & Study", "Ballroom", "Carriage House"),
                "RARE FIND"
        ));

        propertyList.add(new Property(
                4,
                "Skyline Penthouse",
                "One Manhattan West, New York, NY",
                "$8,750,000",
                4.9,
                4, 4, "4,800",
                0,
                "",
                "APARTMENTS",
                R.drawable.property_1,
                "Occupying the entire 62nd floor of Manhattan's most celebrated new tower...",
                "James Whitfield",
                "Manhattan Luxury Division",
                "This penthouse is the pinnacle of New York living.",
                Arrays.asList("Private Elevator", "Concierge 24/7", "Rooftop Pool", "Valet Parking"),
                "EXCLUSIVE LISTING"
        ));
    }

    private void setupRecyclerView() {
        adapter = new PropertyAdapter(requireContext(), propertyList,
                new PropertyAdapter.OnPropertyClickListener() {
                    @Override
                    public void onPropertyClick(Property property) {
                        showPropertyDetail(property);
                    }

                    @Override
                    public void onFavoriteClick(Property property, int position) {
                        // Toggle favorite state
                        boolean newState = !property.isFavorited();
                        property.setFavorited(newState);

                        // Save to FavoritesManager
                        if (newState) {
                            favoritesManager.addFavorite(property.getId());
                            Toast.makeText(requireContext(), "Added to favorites", Toast.LENGTH_SHORT).show();
                        } else {
                            favoritesManager.removeFavorite(property.getId());
                            Toast.makeText(requireContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
                        }

                        // Update UI
                        adapter.notifyItemChanged(position);
                    }
                });

        rvProperties.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvProperties.setAdapter(adapter);
        rvProperties.setNestedScrollingEnabled(false);

        // Stagger entry animation
        rvProperties.setAlpha(0f);
        rvProperties.animate().alpha(1f).setDuration(500).setStartDelay(200).start();
    }

    private void showPropertyDetail(Property property) {
        PropertyDetailBottomSheet sheet =
                PropertyDetailBottomSheet.newInstance(property);
        sheet.show(getChildFragmentManager(), "PropertyDetail");
    }
}