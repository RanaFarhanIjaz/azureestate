package com.example.azureestate.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@IgnoreExtraProperties
public class RealChatMessage {

    @Exclude
    public String messageId;    // set locally from snapshot key

    public String senderId;
    public String text;
    public String type;         // "text" | "image"
    public long   timestamp;
    public boolean read;

    // Required empty constructor for Firebase
    public RealChatMessage() {}

    public RealChatMessage(String senderId, String text, String type) {
        this.senderId  = senderId;
        this.text      = text;
        this.type      = type;
        this.timestamp = System.currentTimeMillis();
        this.read      = false;
    }

    @Exclude
    public String getFormattedTime() {
        if (timestamp == 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.US);
        return sdf.format(new Date(timestamp));
    }

    @Exclude
    public String getFormattedDate() {
        if (timestamp == 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d", Locale.US);
        return sdf.format(new Date(timestamp));
    }
}