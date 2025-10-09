package com.example.final_project.views.adapter;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.R;
import com.example.final_project.models.entity.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatMessages;
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_BOT = 2;

    public ChatAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    @Override
    public int getItemViewType(int position) {
        return chatMessages.get(position).isUser() ? VIEW_TYPE_USER : VIEW_TYPE_BOT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_bot, parent, false);
            return new BotViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);
        String formattedMessage = formatMessage(message.getMessage());

        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).tvMessageUser.setText(
                    Html.fromHtml(formattedMessage, Html.FROM_HTML_MODE_LEGACY)
            );
        } else {
            ((BotViewHolder) holder).tvMessageBot.setText(
                    Html.fromHtml(formattedMessage, Html.FROM_HTML_MODE_LEGACY)
            );
        }
    }


    private String formatMessage(String text) {
        // Thay thế Markdown cơ bản sang HTML
        text = text.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>"); // In đậm
        text = text.replaceAll("\\*(.*?)\\*", "• $1<br>"); // Gạch đầu dòng
        text = text.replace("\n", "<br>");
        return text; // KHÔNG .toString() nữa
    }


    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageUser;
        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageUser = itemView.findViewById(R.id.tvMessageUser);
        }
    }

    static class BotViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageBot;
        BotViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageBot = itemView.findViewById(R.id.tvMessageBot);
        }
    }
}
