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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

public class SupabasePropAdapter
        extends RecyclerView.Adapter<SupabasePropAdapter.PropVH> {

    public interface OnClickListener {
        void onClick(SupabaseManager.ListingData listing);
        default void onFavoriteClick(SupabaseManager.ListingData listing, int position) {}
    }

    private final Context context;
    private List<SupabaseManager.ListingData> items;
    private final OnClickListener listener;

    public SupabasePropAdapter(Context ctx,
                                List<SupabaseManager.ListingData> items,
                                OnClickListener listener) {
        this.context  = ctx;
        this.items    = new ArrayList<>(items);
        this.listener = listener;
    }

    public void updateList(List<SupabaseManager.ListingData> newList) {
        this.items = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public PropVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_property, parent, false);
        return new PropVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PropVH h, int position) {
        SupabaseManager.ListingData d = items.get(position);

        // ── Load image from Supabase Storage URL via Glide ──
        String coverUrl = d.getCoverPhotoUrl();
        if (coverUrl != null && !coverUrl.isEmpty()) {
            Glide.with(context)
                    .load(coverUrl)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.property_1)
                            .error(R.drawable.property_1)
                            .centerCrop())
                    .transition(DrawableTransitionOptions.withCrossFade(300))
                    .into(h.ivPropertyImage);
        } else {
            h.ivPropertyImage.setImageResource(R.drawable.property_1);
        }

        // ── Text fields ──
        h.tvPrice.setText(d.getFormattedPrice());
        h.tvRating.setText(String.valueOf(d.rating));
        h.tvAddress.setText(d.address);
        h.tvBeds.setText(d.beds + " Beds");
        h.tvBaths.setText(d.baths + " Baths");
        h.tvSqft.setText(d.sqft + " sqft");

        // Badge
        if (d.badge != null && !d.badge.isEmpty()) {
            h.tvBadge.setText(d.badge);
            h.tvBadge.setVisibility(View.VISIBLE);
        } else {
            h.tvBadge.setVisibility(View.GONE);
        }

        // Favorite Sync
        boolean isFav = FavoritesManager.getInstance(context).isFavorite(d.id);
        h.ivFavorite.setImageResource(isFav ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);

        // ── Touch: hover overlay + card press ──
        h.ivPropertyImage.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    showOverlay(h.llHoverOverlay, true);
                    scaleCard(h.itemView, true);
                    break;
                case MotionEvent.ACTION_UP:
                    showOverlay(h.llHoverOverlay, false);
                    scaleCard(h.itemView, false);
                    if (event.getAction() == MotionEvent.ACTION_UP)
                        listener.onClick(d);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    showOverlay(h.llHoverOverlay, false);
                    scaleCard(h.itemView, false);
                    break;
            }
            return true;
        });

        h.itemView.setOnClickListener(v -> listener.onClick(d));

        // Favorite
        h.ivFavorite.setOnClickListener(v -> {
            bounceFav(h.ivFavorite);
            listener.onFavoriteClick(d, position);
        });
    }

    @Override public int getItemCount() { return items.size(); }

    private void showOverlay(LinearLayout overlay, boolean show) {
        overlay.setVisibility(View.VISIBLE);
        overlay.animate().alpha(show ? 1f : 0f).setDuration(180)
                .withEndAction(() -> { if (!show) overlay.setVisibility(View.GONE); })
                .start();
    }

    private void scaleCard(View v, boolean pressed) {
        float s = pressed ? 0.97f : 1f;
        AnimatorSet set = new AnimatorSet();
        set.playTogether(ObjectAnimator.ofFloat(v, "scaleX", s),
                         ObjectAnimator.ofFloat(v, "scaleY", s));
        set.setDuration(150).setInterpolator(new DecelerateInterpolator());
        set.start();
    }

    private void bounceFav(ImageView iv) {
        AnimatorSet s = new AnimatorSet();
        AnimatorSet up = new AnimatorSet();
        up.playTogether(ObjectAnimator.ofFloat(iv,"scaleX",1f,1.3f),
                         ObjectAnimator.ofFloat(iv,"scaleY",1f,1.3f));
        up.setDuration(120);
        AnimatorSet dn = new AnimatorSet();
        dn.playTogether(ObjectAnimator.ofFloat(iv,"scaleX",1.3f,1f),
                         ObjectAnimator.ofFloat(iv,"scaleY",1.3f,1f));
        dn.setDuration(100);
        s.playSequentially(up, dn);
        s.start();
    }

    static class PropVH extends RecyclerView.ViewHolder {
        ImageView ivPropertyImage, ivFavorite;
        TextView tvBadge, tvPrice, tvRating, tvAddress, tvBeds, tvBaths, tvSqft;
        LinearLayout llHoverOverlay;

        PropVH(@NonNull View v) {
            super(v);
            ivPropertyImage = v.findViewById(R.id.ivPropertyImage);
            ivFavorite      = v.findViewById(R.id.ivFavorite);
            tvBadge         = v.findViewById(R.id.tvBadge);
            tvPrice         = v.findViewById(R.id.tvPrice);
            tvRating        = v.findViewById(R.id.tvRating);
            tvAddress       = v.findViewById(R.id.tvAddress);
            tvBeds          = v.findViewById(R.id.tvBeds);
            tvBaths         = v.findViewById(R.id.tvBaths);
            tvSqft          = v.findViewById(R.id.tvSqft);
            llHoverOverlay  = v.findViewById(R.id.llHoverOverlay);
        }
    }
}