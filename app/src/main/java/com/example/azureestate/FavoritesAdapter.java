package com.example.azureestate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.azureestate.models.Property;
import java.util.ArrayList;
import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {
    
    private List<Property> favoritesList;
    private OnFavoriteClickListener listener;
    
    public interface OnFavoriteClickListener {
        void onFavoriteClick(Property property);
        void onRemoveFavorite(Property property, int position);
    }
    
    // Constructor with 2 parameters
    public FavoritesAdapter(OnFavoriteClickListener listener) {
        this.favoritesList = new ArrayList<>();
        this.listener = listener;
    }
    
    public void updateList(List<Property> newList) {
        this.favoritesList = newList;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.favorite_item, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Property property = favoritesList.get(position);
        
        holder.tvPrice.setText(property.getPrice());
        holder.tvAddress.setText(property.getAddress());
        holder.tvBeds.setText(property.getBeds() + " Beds");
        holder.tvBaths.setText(property.getBaths() + " Baths");
        holder.tvSqft.setText(property.getSqft() + " sqft");
        
        if (property.getBadge() != null && !property.getBadge().isEmpty()) {
            holder.tvBadge.setVisibility(View.VISIBLE);
            holder.tvBadge.setText(property.getBadge());
        } else {
            holder.tvBadge.setVisibility(View.GONE);
        }
        
        // Set favorite icon to filled
        holder.ivFavorite.setImageResource(R.drawable.ic_favorite_filled);
        holder.ivFavorite.setColorFilter(holder.itemView.getContext().getColor(R.color.teal_primary));
        
        // Remove from favorites on click
        holder.ivFavorite.setOnClickListener(v -> {
            favoritesList.remove(position);
            notifyItemRemoved(position);
            if (listener != null) {
                listener.onRemoveFavorite(property, position);
            }
        });
        
        // Item click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFavoriteClick(property);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return favoritesList.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPrice, tvAddress, tvBeds, tvBaths, tvSqft, tvBadge;
        ImageView ivFavorite;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvBeds = itemView.findViewById(R.id.tvBeds);
            tvBaths = itemView.findViewById(R.id.tvBaths);
            tvSqft = itemView.findViewById(R.id.tvSqft);
            tvBadge = itemView.findViewById(R.id.tvBadge);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
        }
    }
}