package com.example.azureestate;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.azureestate.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final Context context;
    private final List<ChatMessage> messages;

    public ChatAdapter(Context context, List<ChatMessage> messages) {
        this.context  = context;
        this.messages = messages;
    }

    @NonNull @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        holder.bind(msg);
    }

    @Override
    public int getItemCount() { return messages.size(); }

    class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llUserBubble, llAiBubble, llTypingIndicator;
        TextView tvUserMessage, tvUserTime, tvAiMessage, tvAiTime;
        View dot1, dot2, dot3;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            llUserBubble       = itemView.findViewById(R.id.llUserBubble);
            llAiBubble         = itemView.findViewById(R.id.llAiBubble);
            llTypingIndicator  = itemView.findViewById(R.id.llTypingIndicator);
            tvUserMessage      = itemView.findViewById(R.id.tvUserMessage);
            tvUserTime         = itemView.findViewById(R.id.tvUserTime);
            tvAiMessage        = itemView.findViewById(R.id.tvAiMessage);
            tvAiTime           = itemView.findViewById(R.id.tvAiTime);
            dot1               = itemView.findViewById(R.id.dot1);
            dot2               = itemView.findViewById(R.id.dot2);
            dot3               = itemView.findViewById(R.id.dot3);
        }

        void bind(ChatMessage msg) {
            llUserBubble.setVisibility(View.GONE);
            llAiBubble.setVisibility(View.GONE);
            llTypingIndicator.setVisibility(View.GONE);

            switch (msg.getType()) {
                case USER:
                    llUserBubble.setVisibility(View.VISIBLE);
                    tvUserMessage.setText(msg.getText());
                    tvUserTime.setText(msg.getTime());
                    slideIn(llUserBubble, true);
                    break;

                case AI:
                    llAiBubble.setVisibility(View.VISIBLE);
                    tvAiMessage.setText(msg.getText());
                    tvAiTime.setText(msg.getTime());
                    slideIn(llAiBubble, false);
                    break;

                case TYPING:
                    llTypingIndicator.setVisibility(View.VISIBLE);
                    animateTypingDots();
                    break;
            }
        }

        private void slideIn(View v, boolean fromRight) {
            float startX = fromRight ? 60f : -60f;
            v.setTranslationX(startX);
            v.setAlpha(0f);
            v.animate()
                    .translationX(0f).alpha(1f)
                    .setDuration(250)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        private void animateTypingDots() {
            View[] dots = {dot1, dot2, dot3};
            for (int i = 0; i < dots.length; i++) {
                final View dot = dots[i];
                final int delay = i * 180;
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    ObjectAnimator anim = ObjectAnimator.ofFloat(dot, "translationY", 0f, -6f, 0f);
                    anim.setDuration(500);
                    anim.setRepeatCount(ObjectAnimator.INFINITE);
                    anim.setRepeatMode(ObjectAnimator.RESTART);
                    anim.start();
                }, delay);
            }
        }
    }
}