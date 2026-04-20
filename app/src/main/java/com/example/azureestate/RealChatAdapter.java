package com.example.azureestate;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.azureestate.models.RealChatMessage;

import java.util.List;

public class RealChatAdapter extends RecyclerView.Adapter<RealChatAdapter.MsgViewHolder> {

    private static final int TYPE_SENT     = 1;
    private static final int TYPE_RECEIVED = 2;

    private final Context context;
    private final List<RealChatMessage> messages;
    private final String currentUserId;

    public RealChatAdapter(Context context, List<RealChatMessage> messages, String currentUserId) {
        this.context       = context;
        this.messages      = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).senderId != null
                && messages.get(position).senderId.equals(currentUserId)
                ? TYPE_SENT : TYPE_RECEIVED;
    }

    @NonNull @Override
    public MsgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_chat_bubble, parent, false);
        return new MsgViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MsgViewHolder holder, int position) {
        RealChatMessage msg  = messages.get(position);
        boolean isSent       = getItemViewType(position) == TYPE_SENT;
        RealChatMessage prev = position > 0 ? messages.get(position - 1) : null;

        holder.bind(msg, isSent, prev);
    }

    @Override
    public int getItemCount() { return messages.size(); }

    class MsgViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llBubbleWrap;
        TextView tvMessage, tvTime;
        ImageView ivReadReceipt;
        View dateHeader;
        TextView tvDateHeader;

        public MsgViewHolder(@NonNull View itemView) {
            super(itemView);
            llBubbleWrap = itemView.findViewById(R.id.llBubbleWrap);
            tvMessage    = itemView.findViewById(R.id.tvMessage);
            tvTime       = itemView.findViewById(R.id.tvTime);
            ivReadReceipt= itemView.findViewById(R.id.ivReadReceipt);
            dateHeader   = itemView.findViewById(R.id.dateHeaderContainer);
            tvDateHeader = itemView.findViewById(R.id.tvDateHeader);
        }

        void bind(RealChatMessage msg, boolean isSent, RealChatMessage prev) {
            // ── Date header between days ──
            if (prev == null || !sameDay(prev.timestamp, msg.timestamp)) {
                dateHeader.setVisibility(View.VISIBLE);
                tvDateHeader.setText(msg.getFormattedDate());
            } else {
                dateHeader.setVisibility(View.GONE);
            }

            // ── Message text ──
            tvMessage.setText(msg.text != null ? msg.text : "");

            // ── Alignment + colors ──
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 0);

            if (isSent) {
                llBubbleWrap.setGravity(Gravity.END);
                tvMessage.setBackgroundResource(R.drawable.bubble_user);
                tvMessage.setTextColor(0xFFFFFFFF);
                lp.gravity = Gravity.END;
                ivReadReceipt.setVisibility(View.VISIBLE);
                ivReadReceipt.setColorFilter(msg.read ? 0xFF3DB8A8 : 0xFF8AAFC8);
            } else {
                llBubbleWrap.setGravity(Gravity.START);
                tvMessage.setBackgroundResource(R.drawable.bubble_ai);
                tvMessage.setTextColor(0xFF0D1B2A);
                lp.gravity = Gravity.START;
                ivReadReceipt.setVisibility(View.GONE);
            }
            tvMessage.setLayoutParams(lp);

            // ── Time ──
            tvTime.setText(msg.getFormattedTime());
            tvTime.setGravity(isSent ? Gravity.END : Gravity.START);

            // ── Slide-in animation ──
            slideIn(llBubbleWrap, isSent);
        }

        private void slideIn(View v, boolean fromRight) {
            float startX = fromRight ? 40f : -40f;
            v.setTranslationX(startX);
            v.setAlpha(0f);
            v.animate().translationX(0f).alpha(1f).setDuration(220)
                    .setInterpolator(new DecelerateInterpolator()).start();
        }

        private boolean sameDay(long ts1, long ts2) {
            java.util.Calendar c1 = java.util.Calendar.getInstance();
            java.util.Calendar c2 = java.util.Calendar.getInstance();
            c1.setTimeInMillis(ts1);
            c2.setTimeInMillis(ts2);
            return c1.get(java.util.Calendar.DAY_OF_MONTH) == c2.get(java.util.Calendar.DAY_OF_MONTH)
                && c1.get(java.util.Calendar.MONTH) == c2.get(java.util.Calendar.MONTH)
                && c1.get(java.util.Calendar.YEAR) == c2.get(java.util.Calendar.YEAR);
        }
    }
}