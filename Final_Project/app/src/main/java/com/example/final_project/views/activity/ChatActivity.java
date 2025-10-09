package com.example.final_project.views.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.BuildConfig;
import com.example.final_project.R;
import com.example.final_project.helper.GeminiApi;
import com.example.final_project.models.entity.ChatMessage;
import com.example.final_project.views.adapter.ChatAdapter;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChat;
    private EditText etMessage;
    private ImageButton btnSend;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(chatAdapter);
        btnSend.setOnClickListener(v -> sendMessage());

    }

    private void sendMessage() {
        String message = etMessage.getText().toString().trim();
        if (message.isEmpty()) return;

        chatMessages.add(new ChatMessage(message, true));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
        etMessage.setText("");

        ChatMessage thinkingMessage = new ChatMessage("Đang suy nghĩ...", false);
        chatMessages.add(thinkingMessage);
        int thinkingIndex = chatMessages.size() - 1;
        chatAdapter.notifyItemInserted(thinkingIndex);
        recyclerViewChat.scrollToPosition(thinkingIndex);

        new GeminiApi().sendMessage(message, new GeminiApi.Callback() {
            @Override
            public void onSuccess(String reply) {
                // Xóa tin nhắn "Đang suy nghĩ..."
                chatMessages.remove(thinkingIndex);
                chatAdapter.notifyItemRemoved(thinkingIndex);

                // Thêm phản hồi thật
                chatMessages.add(new ChatMessage(reply, false));
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
            }

            @Override
            public void onError(Exception e) {
                chatMessages.remove(thinkingIndex);
                chatAdapter.notifyItemRemoved(thinkingIndex);

                chatMessages.add(new ChatMessage("Có lỗi xảy ra: " + e.getMessage(), false));
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
            }
        });
    }

}
