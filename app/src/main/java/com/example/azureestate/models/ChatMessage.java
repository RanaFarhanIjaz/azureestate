package com.example.azureestate.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatMessage {
    public enum Type { USER, AI, TYPING }

    private String text;
    private Type type;
    private String time;

    public ChatMessage(String text, Type type) {
        this.text = text;
        this.type = type;
        this.time = new SimpleDateFormat("h:mm a", Locale.US).format(new Date());
    }

    public String getText()  { return text; }
    public Type   getType()  { return type; }
    public String getTime()  { return time; }
    public void setText(String text) { this.text = text; }
}