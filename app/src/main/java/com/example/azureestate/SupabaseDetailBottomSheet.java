package com.example.azureestate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;

public class SupabaseDetailBottomSheet extends BottomSheetDialogFragment {

    private SupabaseManager.ListingData listing;

    public static SupabaseDetailBottomSheet newInstance(SupabaseManager.ListingData listing) {
        SupabaseDetailBottomSheet s = new SupabaseDetailBottomSheet();
        s.listing = listing;
        return s;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_property_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (listing == null) return;

        // Expand to full height
        View parent = (View) view.getParent();
        if (parent != null) {
            BottomSheetBehavior<View> b = BottomSheetBehavior.from(parent);
            b.setState(BottomSheetBehavior.STATE_EXPANDED);
            b.setSkipCollapsed(false);
        }

        // Cover image from Supabase Storage
        ImageView ivImage = view.findViewById(R.id.ivDetailImage);
        String cover = listing.getCoverPhotoUrl();
        if (cover != null && ivImage != null) {
            Glide.with(this).load(cover)
                    .transition(DrawableTransitionOptions.withCrossFade(300))
                    .centerCrop().into(ivImage);
        }

        // Texts
        setText(view, R.id.tvDetailTitle,   listing.title);
        setText(view, R.id.tvDetailAddress, listing.address);
        setText(view, R.id.tvDetailPrice,   listing.getFormattedPrice());
        setText(view, R.id.tvDetailBeds,    String.valueOf(listing.beds));
        setText(view, R.id.tvDetailBaths,   String.valueOf(listing.baths));
        setText(view, R.id.tvDetailSqft,    listing.sqft);
        setText(view, R.id.tvDetailGarages, String.valueOf(listing.garages));
        setText(view, R.id.tvDescription,   listing.description);
        setText(view, R.id.tvAgentName,     listing.ownerEmail);
        setText(view, R.id.tvAgentTitle,    "Property Owner");

        // Exclusive tag
        TextView tag = view.findViewById(R.id.tvExclusiveTag);
        if (tag != null) {
            tag.setText(listing.badge != null && !listing.badge.isEmpty()
                    ? listing.badge : "LISTING");
            tag.setVisibility(View.VISIBLE);
        }

        // Get Directions
        View directions = view.findViewById(R.id.btnOpenMap);
        if (directions != null) {
            directions.setOnClickListener(v -> {
                Uri mapUri = Uri.parse("geo:0,0?q=" + Uri.encode(listing.address));
                Intent i = new Intent(Intent.ACTION_VIEW, mapUri);
                i.setPackage("com.google.android.apps.maps");
                if (i.resolveActivity(requireActivity().getPackageManager()) != null)
                    startActivity(i);
                else startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://maps.google.com/?q=" + Uri.encode(listing.address))));
            });
        }

        // Contact Agent → open chat
        Button btnContact = view.findViewById(R.id.btnContactAgent);
        if (btnContact != null) {
            btnContact.setOnClickListener(v -> {
                String myUid = FirebaseAuth.getInstance().getCurrentUser() != null
                        ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
                String chatId = ChatRoomActivity.buildChatId(myUid, listing.ownerId, listing.id);

                Intent intent = new Intent(requireContext(), ChatRoomActivity.class);
                intent.putExtra(ChatRoomActivity.EXTRA_CHAT_ID,         chatId);
                intent.putExtra(ChatRoomActivity.EXTRA_OTHER_USER_ID,   listing.ownerId);
                intent.putExtra(ChatRoomActivity.EXTRA_OTHER_USER_NAME, listing.ownerEmail);
                intent.putExtra(ChatRoomActivity.EXTRA_PROPERTY_TITLE,  listing.title);
                intent.putExtra(ChatRoomActivity.EXTRA_PROPERTY_PRICE,  listing.getFormattedPrice());
                intent.putExtra(ChatRoomActivity.EXTRA_OWNER_PHONE,     listing.ownerPhone);
                startActivity(intent);
            });
        }

        // Schedule Tour → call
        Button btnTour = view.findViewById(R.id.btnScheduleTour);
        if (btnTour != null) {
            btnTour.setOnClickListener(v -> {
                if (listing.ownerPhone != null && !listing.ownerPhone.isEmpty()) {
                    Intent call = new Intent(Intent.ACTION_DIAL);
                    call.setData(Uri.parse("tel:" + listing.ownerPhone.replaceAll("[^+0-9]","")));
                    startActivity(call);
                } else {
                    Toast.makeText(requireContext(), "No phone number available",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setText(View root, int id, String text) {
        TextView tv = root.findViewById(id);
        if (tv != null && text != null) tv.setText(text);
    }

    @Override public int getTheme() { return R.style.BottomSheetDialogTheme; }
}
