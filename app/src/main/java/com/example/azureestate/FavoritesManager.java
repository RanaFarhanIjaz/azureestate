package com.example.azureestate;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class FavoritesManager {
    private static final String PREF_NAME = "favorites_prefs";
    private static final String KEY_FAVORITES = "favorite_properties";
    private static FavoritesManager instance;
    private SharedPreferences sharedPreferences;

    private FavoritesManager(Context context) {
        sharedPreferences = context.getApplicationContext()
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized FavoritesManager getInstance(Context context) {
        if (instance == null) {
            instance = new FavoritesManager(context);
        }
        return instance;
    }

    // Add favorite by int ID
    public void addFavorite(int propertyId) {
        addFavorite(String.valueOf(propertyId));
    }

    public void addFavorite(String propertyId) {
        Set<String> favorites = getFavoritesSet();
        favorites.add(propertyId);
        saveFavoritesSet(favorites);
    }

    // Remove favorite by int ID
    public void removeFavorite(int propertyId) {
        removeFavorite(String.valueOf(propertyId));
    }

    public void removeFavorite(String propertyId) {
        Set<String> favorites = getFavoritesSet();
        favorites.remove(propertyId);
        saveFavoritesSet(favorites);
    }

    // Check favorite by int ID
    public boolean isFavorite(int propertyId) {
        return isFavorite(String.valueOf(propertyId));
    }

    public boolean isFavorite(String propertyId) {
        return getFavoritesSet().contains(propertyId);
    }

    // Toggle favorite
    public void toggleFavorite(int propertyId) {
        if (isFavorite(propertyId)) {
            removeFavorite(propertyId);
        } else {
            addFavorite(propertyId);
        }
    }

    public void toggleFavorite(String propertyId) {
        if (isFavorite(propertyId)) {
            removeFavorite(propertyId);
        } else {
            addFavorite(propertyId);
        }
    }

    // Get all favorite IDs as Strings
    public Set<String> getAllFavorites() {
        return getFavoritesSet();
    }

    // Get all favorite IDs as ints
    public Set<Integer> getAllFavoriteIds() {
        Set<String> stringSet = getFavoritesSet();
        Set<Integer> intSet = new HashSet<>();
        for (String id : stringSet) {
            try {
                intSet.add(Integer.parseInt(id));
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        return intSet;
    }

    public int getFavoritesCount() {
        return getFavoritesSet().size();
    }

    public void clearAllFavorites() {
        saveFavoritesSet(new HashSet<>());
    }

    private Set<String> getFavoritesSet() {
        return new HashSet<>(sharedPreferences.getStringSet(KEY_FAVORITES, new HashSet<>()));
    }

    private void saveFavoritesSet(Set<String> favorites) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(KEY_FAVORITES, favorites);
        editor.apply();
    }
}