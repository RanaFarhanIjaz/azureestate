package com.example.azureestate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.azureestate.models.RealChatMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRoomActivity extends AppCompatActivity {

    // Intent extras
    public static final String EXTRA_CHAT_ID         = "chatId";        // unique room id
    public static final String EXTRA_OTHER_USER_ID   = "otherUserId";
    public static final String EXTRA_OTHER_USER_NAME = "otherUserName";
    public static final String EXTRA_PROPERTY_ID     = "propertyId";
    public static final String EXTRA_PROPERTY_TITLE  = "propertyTitle";
    public static final String EXTRA_PROPERTY_PRICE  = "propertyPrice";
    public static final String EXTRA_OWNER_PHONE     = "ownerPhone";

    // Views
    private RecyclerView rvChatMessages;
    private EditText etChatMessage;
    private CardView btnSendMsg;
    private ImageView ivChatBack, btnCallOwner, btnChatMore, btnAttach;
    private TextView tvOtherName, tvChatSubtitle, tvStripTitle, tvStripPrice, tvViewProperty;

    // Data
    private final List<RealChatMessage> messageList = new ArrayList<>();
    private RealChatAdapter adapter;

    // Firebase
    private DatabaseReference chatRef;
    private ChildEventListener messageListener;
    private String currentUserId;
    private String chatId;
    private String otherUserId;
    private String otherUserName;
    private String propertyTitle;
    private String propertyPrice;
    private String ownerPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        // Read intent extras
        chatId        = getIntent().getStringExtra(EXTRA_CHAT_ID);
        otherUserId   = getIntent().getStringExtra(EXTRA_OTHER_USER_ID);
        otherUserName = getIntent().getStringExtra(EXTRA_OTHER_USER_NAME);
        propertyTitle = getIntent().getStringExtra(EXTRA_PROPERTY_TITLE);
        propertyPrice = getIntent().getStringExtra(EXTRA_PROPERTY_PRICE);
        ownerPhone    = getIntent().getStringExtra(EXTRA_OWNER_PHONE);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        // Firebase Realtime Database reference
        chatRef = FirebaseDatabase.getInstance()
                .getReference("chats")
                .child(chatId)
                .child("messages");

        bindViews();
        setupRecyclerView();
        setupClickListeners();
        listenForMessages();
        markAsRead();
    }

    private void bindViews() {
        rvChatMessages  = findViewById(R.id.rvChatMessages);
        etChatMessage   = findViewById(R.id.etChatMessage);
        btnSendMsg      = findViewById(R.id.btnSendMsg);
        ivChatBack      = findViewById(R.id.ivChatBack);
        btnCallOwner    = findViewById(R.id.btnCallOwner);
        btnChatMore     = findViewById(R.id.btnChatMore);
        btnAttach       = findViewById(R.id.btnAttach);
        tvOtherName     = findViewById(R.id.tvOtherName);
        tvChatSubtitle  = findViewById(R.id.tvChatSubtitle);
        tvStripTitle    = findViewById(R.id.tvStripTitle);
        tvStripPrice    = findViewById(R.id.tvStripPrice);
        tvViewProperty  = findViewById(R.id.tvViewProperty);

        // Populate header
        tvOtherName.setText(otherUserName != null ? otherUserName : "Owner");
        tvChatSubtitle.setText("About: " + (propertyTitle != null ? propertyTitle : "Property"));
        tvStripTitle.setText(propertyTitle != null ? propertyTitle : "");
        tvStripPrice.setText(propertyPrice != null ? propertyPrice : "");
    }

    private void setupRecyclerView() {
        adapter = new RealChatAdapter(this, messageList, currentUserId);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        rvChatMessages.setLayoutManager(lm);
        rvChatMessages.setAdapter(adapter);
    }

    private void setupClickListeners() {
        ivChatBack.setOnClickListener(v -> finish());

        // ── SEND MESSAGE ──
        btnSendMsg.setOnClickListener(v -> {
            String text = etChatMessage.getText().toString().trim();
            if (TextUtils.isEmpty(text)) return;
            sendMessage(text, "text");
            etChatMessage.setText("");
        });

        // ── CALL (opens phone dialer with owner's number) ──
        btnCallOwner.setOnClickListener(v -> {
            if (ownerPhone != null && !ownerPhone.isEmpty()) {
                animatePulse(btnCallOwner);
                // Open phone dialer intent
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + ownerPhone.replaceAll("[^+0-9]", "")));
                startActivity(callIntent);
            } else {
                // Fallback — no phone number stored
                Toast.makeText(this, "Phone number not available for this listing",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // ── ATTACHMENT (send image) ──
        btnAttach.setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            pick.addCategory(Intent.CATEGORY_OPENABLE);
            pick.setType("image/*");
            startActivityForResult(pick, 101);
        });

        // ── VIEW PROPERTY ──
        tvViewProperty.setOnClickListener(v ->
                Toast.makeText(this, "Opening property details…", Toast.LENGTH_SHORT).show());

        // ── MORE OPTIONS ──
        btnChatMore.setOnClickListener(v ->
                Toast.makeText(this, "Options: Block, Report, Clear history", Toast.LENGTH_SHORT).show());
    }

    // ─────────────────────────────────────────────────────────────
    //  Firebase Realtime Database — send message
    // ─────────────────────────────────────────────────────────────
    private void sendMessage(String text, String type) {
        String key = chatRef.push().getKey();
        if (key == null) return;

        Map<String, Object> msg = new HashMap<>();
        msg.put("senderId",   currentUserId);
        msg.put("text",       text);
        msg.put("type",       type);   // "text" | "image"
        msg.put("timestamp",  ServerValue.TIMESTAMP);
        msg.put("read",       false);

        chatRef.child(key).setValue(msg)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Send failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());

        // Update conversation index so inbox shows latest message
        updateConversationIndex(text, type);
    }

    private void updateConversationIndex(String lastMsg, String type) {
        String myId = currentUserId;
        String myName = FirebaseAuth.getInstance().getCurrentUser() != null && FirebaseAuth.getInstance().getCurrentUser().getEmail() != null 
                        ? FirebaseAuth.getInstance().getCurrentUser().getEmail() : "User";
        
        DatabaseReference convRef = FirebaseDatabase.getInstance().getReference("conversations");

        Map<String, Object> base = new HashMap<>();
        base.put("chatId",          chatId);
        base.put("lastMessage",     type.equals("image") ? "📷 Photo" : lastMsg);
        base.put("lastTimestamp",   ServerValue.TIMESTAMP);
        base.put("participants",    java.util.Arrays.asList(myId, otherUserId));
        base.put("propertyTitle",   propertyTitle);
        base.put("propertyPrice",   propertyPrice);
        base.put("ownerPhone",      ownerPhone);
        base.put("unreadFor",       otherUserId);

        // Current User's inbox update
        Map<String, Object> myUpdate = new HashMap<>(base);
        myUpdate.put("otherUserName", otherUserName != null ? otherUserName : "Owner");

        // Other User's inbox update
        Map<String, Object> otherUpdate = new HashMap<>(base);
        otherUpdate.put("otherUserName", myName);

        // Write for both users so their inbox reflects it correctly
        convRef.child(myId).child(chatId).updateChildren(myUpdate);
        convRef.child(otherUserId).child(chatId).updateChildren(otherUpdate);
    }

    // ─────────────────────────────────────────────────────────────
    //  Listen for new messages in real time
    // ─────────────────────────────────────────────────────────────
    private void listenForMessages() {
        messageListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String prev) {
                RealChatMessage msg = snapshot.getValue(RealChatMessage.class);
                if (msg != null) {
                    msg.messageId = snapshot.getKey();
                    messageList.add(msg);
                    adapter.notifyItemInserted(messageList.size() - 1);
                    rvChatMessages.smoothScrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String prev) {
                String key = snapshot.getKey();
                for (int i = 0; i < messageList.size(); i++) {
                    if (key != null && key.equals(messageList.get(i).messageId)) {
                        RealChatMessage updated = snapshot.getValue(RealChatMessage.class);
                        if (updated != null) {
                            updated.messageId = key;
                            messageList.set(i, updated);
                            adapter.notifyItemChanged(i);
                        }
                        break;
                    }
                }
            }

            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String prev) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatRoomActivity.this,
                        "Chat error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        chatRef.orderByChild("timestamp").addChildEventListener(messageListener);
    }

    private void markAsRead() {
        // Mark all messages from other user as read
        chatRef.orderByChild("senderId").equalTo(otherUserId).get()
                .addOnSuccessListener(snapshot -> {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        child.getRef().child("read").setValue(true);
                    }
                });
    }

    // ─────────────────────────────────────────────────────────────
    //  Image attachment result
    // ─────────────────────────────────────────────────────────────
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            // For now show a toast — in production upload to Storage then send URL
            Toast.makeText(this, "Image sharing: upload to Firebase Storage then send URL",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // ─────────────────────────────────────────────────────────────
    private void animatePulse(View v) {
        v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100)
                .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(100).start())
                .start();
    }

    // ─────────────────────────────────────────────────────────────
    /**
     * Static helper — creates a deterministic chatId from two user IDs
     * so the same conversation is always found by both participants.
     * Call this before starting ChatRoomActivity.
     */
    public static String buildChatId(String uid1, String uid2, String propertyId) {
        String sorted = uid1.compareTo(uid2) < 0
                ? uid1 + "_" + uid2 : uid2 + "_" + uid1;
        return sorted + "_" + propertyId;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messageListener != null) chatRef.removeEventListener(messageListener);
    }
}
