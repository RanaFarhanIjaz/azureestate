package com.example.azureestate;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.azureestate.models.ChatMessage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AiChatFragment extends Fragment {

    // ── Replace with your actual Groq API key ──
    // Get one free at: https://console.groq.com
    private static final String GROQ_API_KEY = "";
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    
    // Use a confirmed working model
    private static final String MODEL = "llama-3.3-70b-versatile";  // Changed from llama3-8b-8192

    private static final String SYSTEM_PROMPT =
            "You are Estate AI, a luxury real estate assistant for Azure Estate — " +
                    "a premium property platform specializing in architectural masterpieces. " +
                    "You help users find, evaluate, and understand high-end properties. " +
                    "Be concise, professional, warm, and aspirational in tone. " +
                    "Always respond in 2-4 sentences.";

    // Views
    private RecyclerView rvChat;
    private EditText etChatInput;
    private CardView btnSend;
    private LinearLayout llSuggestions;
    private HorizontalScrollView hsvSuggestions;
    private View btnClearChat;

    // Data
    private final List<ChatMessage> messages = new ArrayList<>();
    private ChatAdapter adapter;
    private final List<JSONObject> conversationHistory = new ArrayList<>();

    // Network
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private boolean isTyping = false;

    // Suggestion prompts
    private static final List<String> SUGGESTIONS = Arrays.asList(
            "Find me a 4-bed house under $3M",
            "Best neighborhoods in LA?",
            "What makes a good investment property?",
            "Compare Villa vs Apartment"
    );

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ai_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Validate API key
        if (GROQ_API_KEY == null || GROQ_API_KEY.isEmpty() || GROQ_API_KEY.equals("YOUR_API_KEY_HERE")) {
            Toast.makeText(getContext(), "Please set your Groq API key", Toast.LENGTH_LONG).show();
            return;
        }
        
        bindViews(view);
        setupRecyclerView();
        setupInput();
        setupSuggestions();
        setupClearButton();
        showWelcomeMessage();
    }

    private void bindViews(View v) {
        rvChat        = v.findViewById(R.id.rvChat);
        etChatInput   = v.findViewById(R.id.etChatInput);
        btnSend       = v.findViewById(R.id.btnSend);
        llSuggestions = v.findViewById(R.id.llSuggestions);
        hsvSuggestions= v.findViewById(R.id.hsvSuggestions);
        btnClearChat  = v.findViewById(R.id.btnClearChat);
    }

    private void setupRecyclerView() {
        adapter = new ChatAdapter(requireContext(), messages);
        LinearLayoutManager lm = new LinearLayoutManager(requireContext());
        lm.setStackFromEnd(true);
        rvChat.setLayoutManager(lm);
        rvChat.setAdapter(adapter);
    }

    private void setupInput() {
        btnSend.setOnClickListener(v -> sendMessage());

        etChatInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void setupSuggestions() {
        llSuggestions.removeAllViews();
        for (String suggestion : SUGGESTIONS) {
            TextView chip = new TextView(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(8);
            chip.setLayoutParams(lp);
            chip.setText(suggestion);
            chip.setTextColor(0xFF3DB8A8);
            chip.setTextSize(12f);
            chip.setBackgroundResource(R.drawable.chip_active);
            chip.setPadding(24, 12, 24, 12);
            chip.setOnClickListener(v -> {
                etChatInput.setText(suggestion);
                sendMessage();
            });
            llSuggestions.addView(chip);
        }
    }

    private void setupClearButton() {
        btnClearChat.setOnClickListener(v -> clearChat());
    }

    private void showWelcomeMessage() {
        String welcome = "Welcome to Estate AI ✦\n\n" +
                "I'm your personal real estate advisor. " +
                "Ask me anything about properties, neighborhoods, pricing, " +
                "architecture, or investment strategy.";
        addMessage(welcome, ChatMessage.Type.AI);
    }

    private void sendMessage() {
        String text = etChatInput.getText().toString().trim();
        if (text.isEmpty() || isTyping) return;

        etChatInput.setText("");
        hideSuggestions();

        // Add user message
        addMessage(text, ChatMessage.Type.USER);

        // Add to history
        try {
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", text);
            conversationHistory.add(userMsg);
        } catch (Exception e) { 
            e.printStackTrace(); 
        }

        // Show typing indicator
        showTyping();

        // Call Groq API
        callGroqApi(text);
    }

    private void callGroqApi(String userText) {
        isTyping = true;

        new Thread(() -> {
            try {
                JSONArray messagesArray = new JSONArray();

                // System message
                JSONObject systemMsg = new JSONObject();
                systemMsg.put("role", "system");
                systemMsg.put("content", SYSTEM_PROMPT);
                messagesArray.put(systemMsg);

                // Conversation history (keep last 10 exchanges)
                int start = Math.max(0, conversationHistory.size() - 10);
                for (int i = start; i < conversationHistory.size(); i++) {
                    messagesArray.put(conversationHistory.get(i));
                }

                // Build request body
                JSONObject requestBody = new JSONObject();
                requestBody.put("model", MODEL);
                requestBody.put("messages", messagesArray);
                requestBody.put("max_tokens", 500);
                requestBody.put("temperature", 0.7);

                RequestBody body = RequestBody.create(
                        requestBody.toString(),
                        MediaType.parse("application/json")
                );

                Request request = new Request.Builder()
                        .url(GROQ_API_URL)
                        .addHeader("Authorization", "Bearer " + GROQ_API_KEY)
                        .addHeader("Content-Type", "application/json")
                        .post(body)
                        .build();

                Response response = httpClient.newCall(request).execute();
                String responseBody = response.body() != null ? response.body().string() : "";

                if (response.isSuccessful()) {
                    JSONObject json = new JSONObject(responseBody);
                    
                    // Check if choices array exists and has content
                    if (json.has("choices") && json.getJSONArray("choices").length() > 0) {
                        String aiText = json
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");

                        // Add AI response to history
                        JSONObject aiMsg = new JSONObject();
                        aiMsg.put("role", "assistant");
                        aiMsg.put("content", aiText);
                        conversationHistory.add(aiMsg);

                        mainHandler.post(() -> {
                            hideTyping();
                            addMessage(aiText.trim(), ChatMessage.Type.AI);
                            isTyping = false;
                        });
                    } else {
                        throw new Exception("No response from AI");
                    }
                } else {
                    // Parse error
                    String errorMsg = parseErrorResponse(responseBody, response.code());
                    mainHandler.post(() -> {
                        hideTyping();
                        addMessage(errorMsg, ChatMessage.Type.AI);
                        isTyping = false;
                    });
                }

            } catch (IOException e) {
                mainHandler.post(() -> {
                    hideTyping();
                    addMessage("Network error. Please check your internet connection.", ChatMessage.Type.AI);
                    isTyping = false;
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    hideTyping();
                    addMessage("Error: " + e.getMessage(), ChatMessage.Type.AI);
                    isTyping = false;
                });
            }
        }).start();
    }

    private String parseErrorResponse(String body, int code) {
        if (code == 400) {
            return "API Error (400): Bad request. Please check your API key or try a different model.\n\nDetails: " + body;
        }
        if (code == 401) return "Invalid API key. Please check your Groq API key.";
        if (code == 429) return "Rate limit reached. Please wait a moment and try again.";
        if (code == 503) return "Groq service is temporarily unavailable. Please try again shortly.";
        return "Request failed (error " + code + "). Please try again.\n\n" + body;
    }

    private void addMessage(String text, ChatMessage.Type type) {
        messages.add(new ChatMessage(text, type));
        adapter.notifyItemInserted(messages.size() - 1);
        rvChat.smoothScrollToPosition(messages.size() - 1);
    }

    private int typingIndex = -1;

    private void showTyping() {
        messages.add(new ChatMessage("", ChatMessage.Type.TYPING));
        typingIndex = messages.size() - 1;
        adapter.notifyItemInserted(typingIndex);
        rvChat.smoothScrollToPosition(typingIndex);
    }

    private void hideTyping() {
        if (typingIndex >= 0 && typingIndex < messages.size()) {
            messages.remove(typingIndex);
            adapter.notifyItemRemoved(typingIndex);
            typingIndex = -1;
        }
    }

    private void hideSuggestions() {
        if (hsvSuggestions.getVisibility() == View.VISIBLE) {
            hsvSuggestions.animate().alpha(0f).setDuration(200)
                    .withEndAction(() -> hsvSuggestions.setVisibility(View.GONE))
                    .start();
        }
    }

    private void clearChat() {
        messages.clear();
        conversationHistory.clear();
        adapter.notifyDataSetChanged();
        hsvSuggestions.setAlpha(1f);
        hsvSuggestions.setVisibility(View.VISIBLE);
        isTyping = false;
        typingIndex = -1;
        showWelcomeMessage();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}