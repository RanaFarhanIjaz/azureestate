package com.example.azureestate;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessagesFragment extends Fragment {

    private RecyclerView rvConversations;
    private LinearLayout llInboxEmpty;
    private TextView tvInboxUnreadCount;
    private EditText etSearchConversations;

    private ConversationAdapter adapter;
    private final List<ConversationItem> allConversations  = new ArrayList<>();
    private final List<ConversationItem> filteredList = new ArrayList<>();

    private DatabaseReference convRef;
    private ValueEventListener convListener;
    private String myUid;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        myUid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        rvConversations    = view.findViewById(R.id.rvConversations);
        llInboxEmpty       = view.findViewById(R.id.llInboxEmpty);
        tvInboxUnreadCount = view.findViewById(R.id.tvInboxUnreadCount);
        etSearchConversations = view.findViewById(R.id.etSearchConversations);

        setupRecyclerView();
        setupSearch();
        listenForConversations();
    }

    // ─────────────────────────────────────────────────────────────
    private void setupRecyclerView() {
        adapter = new ConversationAdapter(filteredList, item -> {
            // Immediately clear unread badge for this conversation
            if (!myUid.isEmpty() && item.chatId != null) {
                FirebaseDatabase.getInstance()
                        .getReference("conversations")
                        .child(myUid)
                        .child(item.chatId)
                        .child("unreadFor")
                        .setValue(null);
                // Update local model so badge disappears instantly without waiting for Firebase
                item.hasUnread = false;
                adapter.notifyDataSetChanged();
            }

            // Open chat room
            Intent intent = new Intent(requireContext(), ChatRoomActivity.class);
            intent.putExtra(ChatRoomActivity.EXTRA_CHAT_ID,         item.chatId);
            intent.putExtra(ChatRoomActivity.EXTRA_OTHER_USER_ID,   item.otherUserId);
            intent.putExtra(ChatRoomActivity.EXTRA_OTHER_USER_NAME, item.otherUserName);
            intent.putExtra(ChatRoomActivity.EXTRA_PROPERTY_TITLE,  item.propertyTitle);
            intent.putExtra(ChatRoomActivity.EXTRA_PROPERTY_PRICE,  item.propertyPrice);
            intent.putExtra(ChatRoomActivity.EXTRA_OWNER_PHONE,     item.ownerPhone);
            startActivity(intent);
        });
        rvConversations.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvConversations.setAdapter(adapter);
    }

    // ─────────────────────────────────────────────────────────────
    //  Live search filter
    // ─────────────────────────────────────────────────────────────
    private void setupSearch() {
        etSearchConversations.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int i, int b, int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                filterConversations(s.toString().trim());
            }
        });
    }

    private void filterConversations(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(allConversations);
        } else {
            String q = query.toLowerCase();
            for (ConversationItem item : allConversations) {
                if ((item.otherUserName  != null && item.otherUserName.toLowerCase().contains(q))
                        || (item.propertyTitle  != null && item.propertyTitle.toLowerCase().contains(q))
                        || (item.lastMessage    != null && item.lastMessage.toLowerCase().contains(q))) {
                    filteredList.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    // ─────────────────────────────────────────────────────────────
    //  Firebase: listen for conversations
    // ─────────────────────────────────────────────────────────────
    private void listenForConversations() {
        if (myUid.isEmpty()) {
            showEmptyState();
            return;
        }

        convRef = FirebaseDatabase.getInstance()
                .getReference("conversations")
                .child(myUid);

        convListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allConversations.clear();
                int unreadCount = 0;
                java.util.HashSet<String> seenChatIds = new java.util.HashSet<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    ConversationItem item = new ConversationItem();
                    item.chatId        = child.child("chatId").getValue(String.class);
                    item.otherUserId   = getOtherUser(child);
                    item.otherUserName = child.child("otherUserName").getValue(String.class);
                    item.propertyTitle = child.child("propertyTitle").getValue(String.class);
                    item.propertyPrice = child.child("propertyPrice").getValue(String.class);
                    item.lastMessage   = child.child("lastMessage").getValue(String.class);
                    item.ownerPhone    = child.child("ownerPhone").getValue(String.class);
                    Long ts = child.child("lastTimestamp").getValue(Long.class);
                    item.lastTimestamp = ts != null ? ts : 0;

                    // Unread: message was sent to me and not yet read
                    String unreadFor = child.child("unreadFor").getValue(String.class);
                    item.hasUnread = myUid.equals(unreadFor);

                    // Filters:
                    // 1. Must have a valid chatId that we haven't seen yet (prevents duplicates)
                    // 2. Must have a lastMessage (someone actually sent a message)
                    // 3. otherUserId must not be empty (prevents self-chats)
                    if (item.chatId != null && !seenChatIds.contains(item.chatId) &&
                        item.lastMessage != null && !item.lastMessage.trim().isEmpty() &&
                        item.otherUserId != null && !item.otherUserId.isEmpty()) {
                        
                        seenChatIds.add(item.chatId);
                        if (item.hasUnread) unreadCount++;
                        allConversations.add(item);
                    }
                }

                // Sort by most recent
                Collections.sort(allConversations,
                        (a, b) -> Long.compare(b.lastTimestamp, a.lastTimestamp));

                filteredList.clear();
                filteredList.addAll(allConversations);
                adapter.notifyDataSetChanged();
                updateEmptyState();
                updateUnreadBadge(unreadCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showEmptyState();
            }
        };

        convRef.addValueEventListener(convListener);
    }

    private String getOtherUser(DataSnapshot snapshot) {
        try {
            for (DataSnapshot p : snapshot.child("participants").getChildren()) {
                String uid = p.getValue(String.class);
                if (uid != null && !uid.equals(myUid)) return uid;
            }
        } catch (Exception ignored) {}
        return "";
    }

    private void updateEmptyState() {
        if (!isAdded()) return;
        boolean empty = filteredList.isEmpty();
        rvConversations.setVisibility(empty ? View.GONE : View.VISIBLE);
        llInboxEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void showEmptyState() {
        if (!isAdded()) return;
        rvConversations.setVisibility(View.GONE);
        llInboxEmpty.setVisibility(View.VISIBLE);
    }

    private void updateUnreadBadge(int count) {
        if (!isAdded()) return;
        if (count > 0) {
            tvInboxUnreadCount.setText(count + " unread");
            tvInboxUnreadCount.setVisibility(View.VISIBLE);
            // Also update main activity badge
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showMessageBadge(count);
            }
        } else {
            tvInboxUnreadCount.setText("All read");
            tvInboxUnreadCount.setTextColor(0xFF5A7A99);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (convRef != null && convListener != null) {
            convRef.removeEventListener(convListener);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Data model
    // ─────────────────────────────────────────────────────────────
    public static class ConversationItem {
        public String  chatId;
        public String  otherUserId;
        public String  otherUserName;
        public String  propertyTitle;
        public String  propertyPrice;
        public String  lastMessage;
        public String  ownerPhone;
        public long    lastTimestamp;
        public boolean hasUnread;
    }

    // ─────────────────────────────────────────────────────────────
    //  Inline adapter
    // ─────────────────────────────────────────────────────────────
    public static class ConversationAdapter
            extends RecyclerView.Adapter<ConversationAdapter.ConvVH> {

        public interface OnConvClick { void onClick(ConversationItem item); }

        private final List<ConversationItem> items;
        private final OnConvClick clickListener;

        public ConversationAdapter(List<ConversationItem> items, OnConvClick clickListener) {
            this.items         = items;
            this.clickListener = clickListener;
        }

        @NonNull @Override
        public ConvVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_conversations, parent, false);
            return new ConvVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ConvVH h, int position) {
            ConversationItem item = items.get(position);

            h.tvName.setText(item.otherUserName != null ? item.otherUserName : "User");
            h.tvPropertyName.setText(item.propertyTitle != null ? item.propertyTitle : "");
            h.tvLastMsg.setText(item.lastMessage != null ? item.lastMessage : "");
            h.tvTime.setText(formatTime(item.lastTimestamp));

            if (item.hasUnread) {
                h.tvUnread.setVisibility(View.VISIBLE);
                h.tvName.setTextColor(0xFF0D1B2A);
                h.tvName.setTypeface(null, Typeface.BOLD);
                h.tvLastMsg.setTextColor(0xFF3A4A5A);
                h.tvLastMsg.setTypeface(null, Typeface.BOLD);
                h.tvTime.setTextColor(0xFF3DB8A8);
            } else {
                h.tvUnread.setVisibility(View.GONE);
                h.tvName.setTextColor(0xFF0D1B2A);
                h.tvName.setTypeface(null, Typeface.NORMAL);
                h.tvLastMsg.setTextColor(0xFF9BAABB);
                h.tvLastMsg.setTypeface(null, Typeface.NORMAL);
                h.tvTime.setTextColor(0xFFB0BFCC);
            }

            // Staggered entrance animation
            h.itemView.setAlpha(0f);
            h.itemView.setTranslationY(30f);
            h.itemView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .setStartDelay((long) position * 50)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            h.itemView.setOnClickListener(v -> {
                v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80)
                        .withEndAction(() ->
                                v.animate().scaleX(1f).scaleY(1f).setDuration(80).start())
                        .start();
                clickListener.onClick(item);
            });
        }

        @Override public int getItemCount() { return items.size(); }

        private String formatTime(long ts) {
            if (ts == 0) return "";
            long now = System.currentTimeMillis();
            long diff = now - ts;
            if (diff < 60_000)                return "now";
            if (diff < 3_600_000)             return (diff / 60_000) + "m";
            if (diff < 86_400_000)            return new SimpleDateFormat("h:mm a", Locale.US).format(new Date(ts));
            return new SimpleDateFormat("MMM d", Locale.US).format(new Date(ts));
        }

        static class ConvVH extends RecyclerView.ViewHolder {
            TextView tvName, tvPropertyName, tvLastMsg, tvTime, tvUnread;
            ConvVH(@NonNull View v) {
                super(v);
                tvName         = v.findViewById(R.id.tvConvName);
                tvPropertyName = v.findViewById(R.id.tvConvPropertyName);
                tvLastMsg      = v.findViewById(R.id.tvConvLastMsg);
                tvTime         = v.findViewById(R.id.tvConvTime);
                tvUnread       = v.findViewById(R.id.tvConvUnread);
            }
        }
    }
}