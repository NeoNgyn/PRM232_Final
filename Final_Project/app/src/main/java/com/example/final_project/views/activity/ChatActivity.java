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

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler; // Thêm import
import android.os.Looper;  // Thêm import
import android.widget.Button; // Thêm import
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout; // Thêm import
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider; // Thêm import quan trọng này
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.R;
import com.example.final_project.helper.GeminiApi;
import com.example.final_project.models.entity.ChatMessage;
import com.example.final_project.models.entity.FoodItem;
import com.example.final_project.models.entity.RecipeData; // Thêm import cho RecipeData
import com.example.final_project.utils.UserSessionManager;
import com.example.final_project.viewmodels.FoodItemViewModel;
import com.example.final_project.viewmodels.RecipeViewModel; // Thêm import cho RecipeViewModel
import com.example.final_project.views.adapter.ChatAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService; // Thêm import
import java.util.concurrent.Executors;   // Thêm import

public class ChatActivity extends AppCompatActivity implements ChatAdapter.OnRecipeSaveListener {

    private RecyclerView recyclerViewChat;
    private EditText etMessage;
    private ImageButton btnSend;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private RecipeViewModel recipeViewModel;

    // Các biến cho nút chat nhanh
    private LinearLayout llQuickReplies;
    private Button btnQuickFridge;
    private Button btnQuickHungry;

    // ExecutorService để chạy tác vụ DB trên luồng nền
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, HomeMenuActivity.class);
            startActivity(intent);
        });

        // --- Ánh xạ các view ---
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        llQuickReplies = findViewById(R.id.llQuickReplies);
        btnQuickFridge = findViewById(R.id.btnQuickFridge);
        btnQuickHungry = findViewById(R.id.btnQuickHungry);
        // --------------------------

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, this);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(chatAdapter);

        // Khởi tạo RecipeViewModel đúng cách
        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);
        observeViewModel(); // Thiết lập observer

        // --- Thiết lập Listener cho các nút ---
        btnSend.setOnClickListener(v -> sendMessage(etMessage.getText().toString()));

        btnQuickFridge.setOnClickListener(v -> {
            addMessageToList("My fridge", true); // Hiển thị tin nhắn người dùng
            showFridgeContents(); // Gọi hàm hiển thị nội dung tủ lạnh
        });

        btnQuickHungry.setOnClickListener(v -> {
            addMessageToList("I'm hungry", true); // Hiển thị tin nhắn người dùng
            //sendMessage("my fridge"); // Gửi "my fridge" để kích hoạt AI gợi ý
            sendMessage("I'm hungry"); // Gửi "my fridge" để kích hoạt AI gợi ý
        });
        // ------------------------------------

        greetUser();
    }

    /**
     * Lấy danh sách đồ trong tủ từ ViewModel và hiển thị lên chat.
     */
    private void showFridgeContents() {
        // Hiển thị tin nhắn chờ (từ bot)
        addMessageToList("Để mình xem trong tủ có gì nhé...", false);

        databaseExecutor.execute(() -> { // Chạy trên luồng nền
            try {
                FoodItemViewModel foodViewModel = new FoodItemViewModel();
                String currentUserId = UserSessionManager.getInstance(ChatActivity.this).getCurrentUserId();
                //String userId = "1"; // Lấy userId hiện tại (cần thay thế bằng logic lấy userId thực tế)
                List<FoodItem> foodItems = foodViewModel.getFoodItemsByUserId(currentUserId);

                // Quay lại luồng chính để cập nhật UI
                mainHandler.post(() -> {
                    // Xóa tin nhắn chờ (tìm và xóa "Để mình xem...") - Tùy chọn
                    removeThinkingMessage(chatMessages.size() - 1); // Xóa tin nhắn cuối cùng nếu là tin chờ

                    if (foodItems == null || foodItems.isEmpty()) {
                        addMessageToList("Tủ lạnh của bạn đang trống.", false);
                    } else {
                        StringBuilder fridgeContent = new StringBuilder("Trong tủ lạnh của bạn hiện có:\n");
                        for (FoodItem item : foodItems) {
                            if (item.getQuantity() > 0 && item.getFoodName() != null && !item.getFoodName().isEmpty()) {
                                fridgeContent.append("• ").append(item.getFoodName()); // Dùng dấu • cho đẹp hơn
                                fridgeContent.append(" (").append(item.getQuantity()).append(")\n"); // Thêm số lượng
                            }
                        }
                        addMessageToList(fridgeContent.toString().trim(), false);
                    }
                });

            } catch (Exception e) {
                // Xử lý lỗi nếu có
                mainHandler.post(() -> {
                    removeThinkingMessage(chatMessages.size() - 1); // Xóa tin nhắn chờ
                    addMessageToList("Xin lỗi, mình không kiểm tra được tủ lạnh lúc này: " + e.getMessage(), false);
                });
                e.printStackTrace();
            }
        });
    }

    /**
     * Thêm tin nhắn vào danh sách và cập nhật RecyclerView.
     */
    private void addMessageToList(String message, boolean isUser) {
        chatMessages.add(new ChatMessage(message, isUser));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        // Cuộn xuống tin nhắn mới nhất một cách mượt mà hơn
        recyclerViewChat.smoothScrollToPosition(chatMessages.size() - 1);
    }

    /**
     * Gửi tin nhắn đến AI hoặc xử lý logic đặc biệt ("my fridge").
     * @param messageText Nội dung tin nhắn cần gửi.
     */
    private void sendMessage(String messageText) {
        final String message = messageText.trim();
        if (message.isEmpty()) return;

        // Chỉ thêm tin nhắn người dùng nếu nó chưa được thêm (tránh lặp khi bấm nút nhanh)
        if (chatMessages.isEmpty() || !chatMessages.get(chatMessages.size()-1).isUser() || !chatMessages.get(chatMessages.size()-1).getMessage().equals(message)) {
            addMessageToList(message, true);
        }

        etMessage.setText(""); // Luôn xóa ô nhập liệu sau khi gửi

        // Hiển thị tin nhắn "Đang suy nghĩ..."
        ChatMessage thinkingMessage = new ChatMessage("Đang suy nghĩ...", false);
        chatMessages.add(thinkingMessage);
        int thinkingIndex = chatMessages.size() - 1;
        chatAdapter.notifyItemInserted(thinkingIndex);
        recyclerViewChat.smoothScrollToPosition(thinkingIndex);

        String lowerCaseMessage = message.toLowerCase();

        // Xử lý logic AI cho "my fridge" / "I'm hungry"
        if (lowerCaseMessage.equals("my fridge")|| lowerCaseMessage.equals("i'm hungry")) {
            databaseExecutor.execute(() -> { // Lấy dữ liệu tủ lạnh trên luồng nền
                try {
                    FoodItemViewModel foodViewModel = new FoodItemViewModel();
                    String currentUserId = UserSessionManager.getInstance(ChatActivity.this).getCurrentUserId();
                    //String userId = "2"; // Lấy userId hiện tại
                    List<FoodItem> foodItems = foodViewModel.getFoodItemsByUserId(currentUserId);

                    mainHandler.post(() -> { // Quay lại luồng chính để xử lý kết quả
                        if (foodItems == null || foodItems.isEmpty()) {
                            removeThinkingMessage(thinkingIndex);
                            addMessageToList("Tủ lạnh trống nên mình chưa gợi ý được món nào.", false);
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
                            removeThinkingMessage(thinkingIndex);
                            addMessageToList("Không có nguyên liệu nào hợp lệ trong tủ lạnh để gợi ý.", false);
                            return;
                        }

                        // Tạo prompt và gọi API Gemini
                        String aiPrompt =
                                "Bạn là một đầu bếp chuyên nghiệp. Dựa vào nguyên liệu sau: " + ing + ". " +
                                        "Hãy gợi ý chính xác 4 món ăn. " +
                                        "QUAN TRỌNG: Với mỗi món, hãy trả lời theo ĐÚNG định dạng sau, KHÔNG thêm bất kỳ giải thích nào khác:\n" +
                                        "## [Số thứ tự]. [Tên món ăn]\n" +
                                        "@@nguyenlieu [Liệt kê nguyên liệu cần thêm]\n" +
                                        "@@congthuc [Liệt kê các bước công thức]\n" +
                                        "@@dinhduong [Ghi thông tin dinh dưỡng ước tính, ví dụ: Khoảng 450 calo, giàu protein]\n" +
                                        "@@thoigian [Ghi thời gian nấu]\n" +
                                        "---";
                        callGeminiApi(aiPrompt, thinkingIndex);
                    });

                } catch (Exception e) {
                    mainHandler.post(() -> {
                        removeThinkingMessage(thinkingIndex);
                        addMessageToList("Lỗi khi lấy dữ liệu tủ lạnh: " + e.getMessage(), false);
                    });
                }
            });
        } else {
            // Xử lý các tin nhắn văn bản thông thường khác
            callGeminiApi(message, thinkingIndex);
        }
    }

    /**
     * Gọi Gemini API và xử lý kết quả trả về.
     * @param prompt Câu lệnh gửi cho AI.
     * @param thinkingIndex Vị trí của tin nhắn "Đang suy nghĩ..." để xóa sau khi có kết quả.
     */
    private void callGeminiApi(String prompt, final int thinkingIndex) { // thinkingIndex nên là final
        new GeminiApi().sendMessage(prompt, new GeminiApi.Callback() {
            @Override
            public void onSuccess(String reply) {
                mainHandler.post(() -> {
                    removeThinkingMessage(thinkingIndex);
                    addMessageToList(reply, false);
                });
            }

            @Override
            public void onError(Exception e) {
                mainHandler.post(() -> {
                    removeThinkingMessage(thinkingIndex);
                    addMessageToList("Lỗi Gemini: " + e.getMessage(), false);
                });
            }
        });
    }

    /**
     * Xóa tin nhắn "Đang suy nghĩ..." khỏi danh sách và cập nhật RecyclerView.
     * @param index Vị trí của tin nhắn cần xóa.
     */
    private void removeThinkingMessage(int index) {
        // Kiểm tra index hợp lệ và nội dung tin nhắn trước khi xóa
        if (index >= 0 && index < chatMessages.size()) {
            ChatMessage msg = chatMessages.get(index);
            if (msg != null && !msg.isUser() && msg.getMessage().equals("Đang suy nghĩ...")) {
                chatMessages.remove(index);
                chatAdapter.notifyItemRemoved(index);
                // Không cần cuộn lại ở đây
            }
        }
    }

    /**
     * Thiết lập observer để theo dõi trạng thái lưu recipe từ ViewModel.
     */
    private void observeViewModel() {
        recipeViewModel.saveStatus.observe(this, success -> {
            if (success == null) return;
            if (success) {
                Toast.makeText(this, "Lưu vào DB thành công!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Lỗi: Không thể lưu vào DB.", Toast.LENGTH_LONG).show();
            }
            recipeViewModel.resetSaveStatus();
        });
    }

    /**
     * Hiển thị tin nhắn chào mừng ban đầu.
     */
    private void greetUser() {
        String[] greetings = {
                "Xin chào! Mình là MealTime 🍽️ – trợ lý gợi ý món ăn của bạn hôm nay!",
                "Chào mừng bạn đến với MealTime 🤖! Muốn mình gợi ý bữa ăn ngon miệng chứ?",
                "Hey! Đây là MealTime 😋 – cùng khám phá món ăn thú vị nào!",
                "Xin chào, mình là MealTime AI. Hãy nói mình biết bạn có gì trong tủ lạnh nhé!"
        };
        String greeting = greetings[(int) (Math.random() * greetings.length)];
        addMessageToList(greeting, false); // Sử dụng hàm tiện ích
    }

    /**
     * Xử lý sự kiện khi người dùng nhấn nút "Lưu món ăn" trên thẻ công thức.
     * @param recipe Dữ liệu của món ăn cần lưu.
     */
    @Override
    public void onSaveRecipeClicked(RecipeData recipe) {
        String currentUserId = UserSessionManager.getInstance(ChatActivity.this).getCurrentUserId();
        //String userId = "2"; // Lấy userId hiện tại
        recipeViewModel.saveRecipeToDatabase(recipe, currentUserId);
        Toast.makeText(this, "Đã gửi yêu cầu lưu món: " + recipe.getTitle(), Toast.LENGTH_SHORT).show();
    }
}

