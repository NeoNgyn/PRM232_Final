//package com.example.final_project.views.activity;
//
//import android.os.Bundle;
//import android.util.Log;
//import android.view.inputmethod.EditorInfo;
//import android.widget.EditText;
//import android.widget.ImageButton;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.final_project.BuildConfig;
//import com.example.final_project.R;
//import com.example.final_project.helper.GeminiApi;
//import com.example.final_project.models.entity.ChatMessage;
//import com.example.final_project.models.entity.FoodItem;
//import com.example.final_project.viewmodels.FoodItemViewModel;
//import com.example.final_project.views.adapter.ChatAdapter;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class ChatActivity extends AppCompatActivity {
//
//    private RecyclerView recyclerViewChat;
//    private EditText etMessage;
//    private ImageButton btnSend;
//    private List<ChatMessage> chatMessages;
//    private ChatAdapter chatAdapter;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_chat);
//
//        recyclerViewChat = findViewById(R.id.recyclerViewChat);
//        etMessage = findViewById(R.id.etMessage);
//        btnSend = findViewById(R.id.btnSend);
//
//        chatMessages = new ArrayList<>();
//        chatAdapter = new ChatAdapter(chatMessages);
//        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
//        recyclerViewChat.setAdapter(chatAdapter);
//        btnSend.setOnClickListener(v -> sendMessage());
//
//        greetUser();
//    }
//
//    private void sendMessage() {
//        String message = etMessage.getText().toString().trim();
//        if (message.isEmpty()) return;
//
//        chatMessages.add(new ChatMessage(message, true));
//        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
//        recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
//        etMessage.setText("");
//
//        ChatMessage thinkingMessage = new ChatMessage("Đang suy nghĩ...", false);
//        chatMessages.add(thinkingMessage);
//        int thinkingIndex = chatMessages.size() - 1;
//        chatAdapter.notifyItemInserted(thinkingIndex);
//        recyclerViewChat.scrollToPosition(thinkingIndex);
//        String m = message.toLowerCase().trim();
//        if (m.contains("tủ lạnh") || m.contains("tu lanh") || m.contains("my fridge")) {
//            new Thread(() -> {
//                try {
//                    FoodItemViewModel viewModel = new FoodItemViewModel();
//                    String userId = "2"; // sau này lấy từ session đăng nhập
//                    List<FoodItem> foodItems = viewModel.getFoodItemsByUserId(userId);
//
//                    if (foodItems == null || foodItems.isEmpty()) {
//                        runOnUiThread(() -> {
//                            chatMessages.remove(thinkingIndex);
//                            chatAdapter.notifyItemRemoved(thinkingIndex);
//
//                            chatMessages.add(new ChatMessage(
//                                    "Mình chưa thấy nguyên liệu nào trong tủ lạnh của bạn. " +
//                                            "Bạn hãy thêm nguyên liệu vào kho trước (Ví dụ: trứng 6, cà chua 3, mì gói 2), rồi nhắn lại **my fridge** nhé.",
//                                    false));
//                            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
//                            recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
//                        });
//                        return;
//                    }
//
//                    StringBuilder ingredients = new StringBuilder();
//                    for (FoodItem item : foodItems) {
//                        if (item.getQuantity() > 0 && item.getFoodName() != null && !item.getFoodName().isEmpty()) {
//                            ingredients.append(item.getFoodName())
//                                    .append(" (").append(item.getQuantity()).append("), ");
//                        }
//                    }
//                    String ing = ingredients.toString().replaceAll(",\\s*$", "");
//
//                    if (ing.isEmpty()) { /* xử lý như bước (2) */ return; }
//
////                    String aiPrompt =
////                            "Bạn là đầu bếp. Dựa trên các nguyên liệu trong tủ lạnh: " + ing + ". " +
////                                    "Hãy gợi ý đúng 3 món ăn. Với mỗi món, liệt kê: 1) Tên món, 2) Nguyên liệu cần thêm (nếu thiếu), " +
////                                    "3) Công thức tóm tắt 4-6 bước, 4) Thời gian nấu ước tính.";
//                    String aiPrompt =
//                            "Bạn là một đầu bếp chuyên nghiệp. Dựa vào nguyên liệu sau: " + ing + ". " +
//                                    "Hãy gợi ý chính xác 3 món ăn. " +
//                                    "QUAN TRỌNG: Với mỗi món, hãy trả lời theo ĐÚNG định dạng sau, KHÔNG thêm bất kỳ giải thích nào khác:\n" +
//                                    "## [Số thứ tự]. [Tên món ăn]\n" +
//                                    "@@nguyenlieu [Liệt kê nguyên liệu cần thêm]\n" +
//                                    "@@congthuc [Liệt kê các bước công thức]\n" +
//                                    "@@thoigian [Ghi thời gian nấu]\n" +
//                                    "---";
//
//                    new GeminiApi().sendMessage(aiPrompt, new GeminiApi.Callback() {
//                        @Override
//                        public void onSuccess(String reply) {
//                            runOnUiThread(() -> {
//                                chatMessages.remove(thinkingIndex);
//                                chatAdapter.notifyItemRemoved(thinkingIndex);
//
//                                chatMessages.add(new ChatMessage(reply, false));
//                                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
//                                recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
//                            });
//                        }
//
//
//                        @Override
//                        public void onError(Exception e) {
//                            runOnUiThread(() -> {
//                                chatMessages.remove(thinkingIndex);
//                                chatAdapter.notifyItemRemoved(thinkingIndex);
//                                chatMessages.add(new ChatMessage("Lỗi Gemini: " + e.getMessage(), false));
//                                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
//                            });
//                        }
//                    });
//
//                } catch (Exception e) {
//                    runOnUiThread(() -> {
//                        chatMessages.remove(thinkingIndex);
//                        chatAdapter.notifyItemRemoved(thinkingIndex);
//                        chatMessages.add(new ChatMessage("Lỗi lấy dữ liệu: " + e.getMessage(), false));
//                        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
//                    });
//                }
//            }).start();
//        } else {
//            // Xử lý prompt thường
//            new GeminiApi().sendMessage(message, new GeminiApi.Callback() {
//                @Override
//                public void onSuccess(String reply) {
//                    chatMessages.remove(thinkingIndex);
//                    chatAdapter.notifyItemRemoved(thinkingIndex);
//
//                    chatMessages.add(new ChatMessage(reply, false));
//                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
//                    recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
//                }
//
//                @Override
//                public void onError(Exception e) {
//                    chatMessages.remove(thinkingIndex);
//                    chatAdapter.notifyItemRemoved(thinkingIndex);
//                    chatMessages.add(new ChatMessage("Có lỗi xảy ra: " + e.getMessage(), false));
//                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
//                }
//            });
//        }
//    }
//
//    private void greetUser() {
//        String[] greetings = {
//                "Xin chào! Mình là MealTime 🍽️ – trợ lý gợi ý món ăn của bạn hôm nay!",
//                "Chào mừng bạn đến với MealTime 🤖! Muốn mình gợi ý bữa ăn ngon miệng chứ?",
//                "Hey! Đây là MealTime 😋 – cùng khám phá món ăn thú vị nào!",
//                "Xin chào, mình là MealTime AI. Hãy nói mình biết bạn có gì trong tủ lạnh nhé!"
//        };
//
//        String greeting = greetings[(int) (Math.random() * greetings.length)];
//        chatMessages.add(new ChatMessage(greeting, false));
//        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
//        recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
//    }
//
//}


package com.example.final_project.views.activity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider; // Thêm import quan trọng này
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.R;
import com.example.final_project.helper.GeminiApi;
import com.example.final_project.models.entity.ChatMessage;
import com.example.final_project.models.entity.FoodItem;
import com.example.final_project.models.entity.RecipeData;
import com.example.final_project.viewmodels.FoodItemViewModel;
import com.example.final_project.viewmodels.RecipeViewModel;
import com.example.final_project.views.adapter.ChatAdapter;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements ChatAdapter.OnRecipeSaveListener {

    private RecyclerView recyclerViewChat;
    private EditText etMessage;
    private ImageButton btnSend;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private RecipeViewModel recipeViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, this);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(chatAdapter);

        // Khởi tạo RecipeViewModel đúng cách, gắn với vòng đời của Activity
        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);

        // (Khuyến nghị) Theo dõi kết quả lưu từ ViewModel để đưa ra thông báo chính xác
        recipeViewModel.saveStatus.observe(this, success -> {
            if (success == null) return; // Bỏ qua giá trị null ban đầu

            if (success) {
                // Toast này chỉ hiển thị khi ViewModel xác nhận đã lưu vào DB thành công
                Toast.makeText(this, "Lưu vào DB thành công!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Lỗi: Không thể lưu vào DB.", Toast.LENGTH_LONG).show();
            }
            // Reset trạng thái để không hiển thị lại Toast khi xoay màn hình
            recipeViewModel.resetSaveStatus();
        });


        btnSend.setOnClickListener(v -> sendMessage());
        greetUser();
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

        String m = message.toLowerCase().trim();
        if (m.contains("tủ lạnh") || m.contains("tu lanh") || m.contains("my fridge")) {
            new Thread(() -> {
                try {
                    FoodItemViewModel viewModel = new FoodItemViewModel();
                    String userId = "2"; // Sau này lấy từ session đăng nhập
                    List<FoodItem> foodItems = viewModel.getFoodItemsByUserId(userId);

                    if (foodItems == null || foodItems.isEmpty()) {
                        runOnUiThread(() -> {
                            chatMessages.remove(thinkingIndex);
                            chatAdapter.notifyItemRemoved(thinkingIndex);
                            chatMessages.add(new ChatMessage(
                                    "Mình chưa thấy nguyên liệu nào trong tủ lạnh của bạn. Hãy thêm vào nhé!",
                                    false));
                            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                        });
                        return;
                    }

                    StringBuilder ingredients = new StringBuilder();
                    for (FoodItem item : foodItems) {
                        if (item.getQuantity() > 0 && item.getFoodName() != null && !item.getFoodName().isEmpty()) {
                            ingredients.append(item.getFoodName())
                                    .append(" (").append(item.getQuantity()).append("), ");
                        }
                    }
                    String ing = ingredients.toString().replaceAll(",\\s*$", "");

                    if (ing.isEmpty()) {
                        runOnUiThread(()-> {
                            chatMessages.remove(thinkingIndex);
                            chatAdapter.notifyItemRemoved(thinkingIndex);
                            chatMessages.add(new ChatMessage("Không có nguyên liệu nào hợp lệ trong tủ lạnh.", false));
                            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                        });
                        return;
                    }

                    String aiPrompt =
                            "Bạn là một đầu bếp chuyên nghiệp. Dựa vào nguyên liệu sau: " + ing + ". " +
                                    "Hãy gợi ý chính xác 3 món ăn. " +
                                    "QUAN TRỌNG: Với mỗi món, hãy trả lời theo ĐÚNG định dạng sau, KHÔNG thêm bất kỳ giải thích nào khác:\n" +
                                    "## [Số thứ tự]. [Tên món ăn]\n" +
                                    "@@nguyenlieu [Liệt kê nguyên liệu cần thêm]\n" +
                                    "@@congthuc [Liệt kê các bước công thức]\n" +
                                    "@@thoigian [Ghi thời gian nấu]\n" +
                                    "---";

                    new GeminiApi().sendMessage(aiPrompt, new GeminiApi.Callback() {
                        @Override
                        public void onSuccess(String reply) {
                            runOnUiThread(() -> {
                                chatMessages.remove(thinkingIndex);
                                chatAdapter.notifyItemRemoved(thinkingIndex);
                                chatMessages.add(new ChatMessage(reply, false));
                                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            runOnUiThread(() -> {
                                chatMessages.remove(thinkingIndex);
                                chatAdapter.notifyItemRemoved(thinkingIndex);
                                chatMessages.add(new ChatMessage("Lỗi Gemini: " + e.getMessage(), false));
                                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                            });
                        }
                    });

                } catch (Exception e) {
                    runOnUiThread(() -> {
                        chatMessages.remove(thinkingIndex);
                        chatAdapter.notifyItemRemoved(thinkingIndex);
                        chatMessages.add(new ChatMessage("Lỗi lấy dữ liệu: " + e.getMessage(), false));
                        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    });
                }
            }).start();
        } else {
            // Xử lý prompt thường
            new GeminiApi().sendMessage(message, new GeminiApi.Callback() {
                @Override
                public void onSuccess(String reply) {
                    runOnUiThread(() -> {
                        chatMessages.remove(thinkingIndex);
                        chatAdapter.notifyItemRemoved(thinkingIndex);
                        chatMessages.add(new ChatMessage(reply, false));
                        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    });
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> {
                        chatMessages.remove(thinkingIndex);
                        chatAdapter.notifyItemRemoved(thinkingIndex);
                        chatMessages.add(new ChatMessage("Có lỗi xảy ra: " + e.getMessage(), false));
                        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    });
                }
            });
        }
    }

    private void greetUser() {
        String[] greetings = {
                "Xin chào! Mình là MealTime 🍽️ – trợ lý gợi ý món ăn của bạn hôm nay!",
                "Chào mừng bạn đến với MealTime 🤖! Muốn mình gợi ý bữa ăn ngon miệng chứ?",
                "Hey! Đây là MealTime 😋 – cùng khám phá món ăn thú vị nào!",
                "Xin chào, mình là MealTime AI. Hãy nói mình biết bạn có gì trong tủ lạnh nhé!"
        };

        String greeting = greetings[(int) (Math.random() * greetings.length)];
        chatMessages.add(new ChatMessage(greeting, false));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
    }

    @Override
    public void onSaveRecipeClicked(RecipeData recipe) {
        // Gửi yêu cầu lưu tới ViewModel
        String userId = "2"; // Lấy userId từ session/login
        recipeViewModel.saveRecipeToDatabase(recipe, userId);

        // Hiển thị thông báo phản hồi ngay lập tức cho người dùng
        Toast.makeText(this, "Đã gửi yêu cầu lưu món: " + recipe.getTitle(), Toast.LENGTH_SHORT).show();
    }
}
