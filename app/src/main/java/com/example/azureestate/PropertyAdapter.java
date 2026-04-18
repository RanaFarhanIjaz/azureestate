package com.example.azureestate;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.azureestate.models.Property;

import java.util.ArrayList;
import java.util.List;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder> {

    public interface OnPropertyClickListener {
        void onPropertyClick(Property property);
        void onFavoriteClick(Property property, int position);
    }

    private final Context context;
    private List<Property> properties;
    private final OnPropertyClickListener listener;

    public PropertyAdapter(Context context, List<Property> properties,
                           OnPropertyClickListener listener) {
        this.context = context;
        this.properties = new ArrayList<>(properties);
        this.listener = listener;
    }

    public void updateList(List<Property> newList) {
        this.properties = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PropertyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_property, parent, false);
        return new PropertyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PropertyViewHolder holder, int position) {
        Property property = properties.get(position);
        holder.bind(property, position);
    }

    @Override
    public int getItemCount() {
        return properties.size();
    }

    class PropertyViewHolder extends RecyclerView.ViewHolder {

        ImageView ivPropertyImage, ivFavorite;
        TextView tvBadge, tvPrice, tvRating, tvAddress, tvBeds, tvBaths, tvSqft;
        LinearLayout llHoverOverlay;

        public PropertyViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPropertyImage  = itemView.findViewById(R.id.ivPropertyImage);
            ivFavorite       = itemView.findViewById(R.id.ivFavorite);
            tvBadge          = itemView.findViewById(R.id.tvBadge);
            tvPrice          = itemView.findViewById(R.id.tvPrice);
            tvRating         = itemView.findViewById(R.id.tvRating);
            tvAddress        = itemView.findViewById(R.id.tvAddress);
            tvBeds           = itemView.findViewById(R.id.tvBeds);
            tvBaths          = itemView.findViewById(R.id.tvBaths);
            tvSqft           = itemView.findViewById(R.id.tvSqft);
            llHoverOverlay   = itemView.findViewById(R.id.llHoverOverlay);
        }

        void bind(Property property, int position) {
            // Image
            ivPropertyImage.setImageResource(property.getImageResId());

            // Badge
            if (property.getBadge() != null && !property.getBadge().isEmpty()) {
                tvBadge.setText(property.getBadge());
                tvBadge.setVisibility(View.VISIBLE);
            } else {
                tvBadge.setVisibility(View.GONE);
            }

            // Text fields
            tvPrice.setText(property.getPrice());
            tvRating.setText(String.valueOf(property.getRating()));
            tvAddress.setText(property.getAddress());
            tvBeds.setText(property.getBeds() + " Beds");
            tvBaths.setText(property.getBaths() + " Baths");
            tvSqft.setText(property.getSqft() + " sqft");

            // Favorite state
            updateFavoriteIcon(property.isFavorited());

            // ── Hover (touch) overlay on the card image ──
            ivPropertyImage.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        showHoverOverlay(true);
                        animateCardPress(true);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        showHoverOverlay(false);
                        animateCardPress(false);
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            listener.onPropertyClick(property);
                        }
                        break;
                }
                return true;
            });

            // Full card click → detail bottom sheet
            itemView.setOnClickListener(v -> listener.onPropertyClick(property));

            // Favorite button
            ivFavorite.setOnClickListener(v -> {
                animateFavorite(ivFavorite);
                listener.onFavoriteClick(property, position);
                updateFavoriteIcon(property.isFavorited());
            });
        }

        private void showHoverOverlay(boolean show) {
            if (show) {
                llHoverOverlay.setVisibility(View.VISIBLE);
                llHoverOverlay.setAlpha(0f);
                llHoverOverlay.animate().alpha(1f).setDuration(180).start();
            } else {
                llHoverOverlay.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .withEndAction(() -> llHoverOverlay.setVisibility(View.GONE))
                        .start();
            }
        }

        private void animateCardPress(boolean pressed) {
            float scale = pressed ? 0.97f : 1.0f;
            AnimatorSet set = new AnimatorSet();
            set.playTogether(
                    ObjectAnimator.ofFloat(itemView, "scaleX", scale),
                    ObjectAnimator.ofFloat(itemView, "scaleY", scale)
            );
            set.setDuration(150);
            set.setInterpolator(new DecelerateInterpolator());
            set.start();
        }

        private void animateFavorite(ImageView iv) {
            AnimatorSet set = new AnimatorSet();
            set.playSequentially(
                    scaleAnim(iv, 1.0f, 1.3f, 120),
                    scaleAnim(iv, 1.3f, 1.0f, 100)
            );
            set.start();
        }

        private AnimatorSet scaleAnim(View v, float from, float to, long duration) {
            AnimatorSet s = new AnimatorSet();
            s.playTogether(
                    ObjectAnimator.ofFloat(v, "scaleX", from, to),
                    ObjectAnimator.ofFloat(v, "scaleY", from, to)
            );
            s.setDuration(duration);
            return s;
        }

        private void updateFavoriteIcon(boolean favorited) {
            ivFavorite.setImageResource(
                    favorited ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border
            );
        }
    }
}