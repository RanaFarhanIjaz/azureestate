package com.example.azureestate.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.azureestate.R;
import com.example.azureestate.models.Property;
import java.util.ArrayList;
import java.util.List;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.ViewHolder> {
    
    private List<Property> properties;
    private OnPropertyClickListener listener;
    
    public interface OnPropertyClickListener {
        void onPropertyClick(Property property);
        void onFavoriteClick(Property property, int position);
    }
    
    public PropertyAdapter(List<Property> properties, OnPropertyClickListener listener) {
        this.properties = new ArrayList<>(properties);
        this.listener = listener;
    }
    
    // ADD THIS METHOD - updateList
    public void updateList(List<Property> newList) {
        this.properties.clear();
        this.properties.addAll(newList);
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_property, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Property property = properties.get(position);
        holder.bind(property);
    }
    
    @Override
    public int getItemCount() {
        return properties.size();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPrice, tvAddress, tvBeds, tvBaths, tvSqft;
        ImageView ivFavorite;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvBeds = itemView.findViewById(R.id.tvBeds);
            tvBaths = itemView.findViewById(R.id.tvBaths);
            tvSqft = itemView.findViewById(R.id.tvSqft);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
        }
        
        void bind(Property property) {
            tvPrice.setText(property.getPrice());
            tvAddress.setText(property.getAddress());
            tvBeds.setText(property.getBeds() + " Beds");
            tvBaths.setText(property.getBaths() + " Baths");
            tvSqft.setText(property.getSqft() + " sqft");
            
            if (property.isFavorited()) {
                ivFavorite.setImageResource(R.drawable.ic_favorite_filled);
                ivFavorite.setColorFilter(itemView.getContext().getColor(R.color.teal_primary));
            } else {
                ivFavorite.setImageResource(R.drawable.ic_favorite);
                ivFavorite.setColorFilter(itemView.getContext().getColor(R.color.text_secondary));
            }
            
            ivFavorite.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFavoriteClick(property, getAdapterPosition());
                }
            });
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPropertyClick(property);
                }
            });
        }
    }
}