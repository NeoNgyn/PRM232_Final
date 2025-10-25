//package com.example.final_project.views.adapter;
//
//import android.text.Html;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.os.Build;
//import androidx.annotation.NonNull;
//import androidx.core.text.HtmlCompat;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.bumptech.glide.Glide;
//import com.example.final_project.R;
//import com.example.final_project.models.entity.ChatMessage;
//
//import java.util.List;
//
//public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//
//    private final List<ChatMessage> chatMessages;
//    private static final int VIEW_TYPE_USER = 1;
//    private static final int VIEW_TYPE_BOT = 2;
//
//    public ChatAdapter(List<ChatMessage> chatMessages) {
//        this.chatMessages = chatMessages;
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//        return chatMessages.get(position).isUser() ? VIEW_TYPE_USER : VIEW_TYPE_BOT;
//    }
//
//    @NonNull
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        if (viewType == VIEW_TYPE_USER) {
//            View view = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.item_chat_user, parent, false);
//            return new UserViewHolder(view);
//        } else {
//            View view = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.item_chat_bot, parent, false);
//            return new BotViewHolder(view);
//        }
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        ChatMessage message = chatMessages.get(position);
//        String formattedMessage = formatMessage(message.getMessage());
//
//        if (holder instanceof UserViewHolder) {
//            ((UserViewHolder) holder).tvMessageUser.setText(
//                    Html.fromHtml(formattedMessage, Html.FROM_HTML_MODE_LEGACY)
//            );
//        } else {
//            ((BotViewHolder) holder).tvMessageBot.setText(
//                    Html.fromHtml(formattedMessage, Html.FROM_HTML_MODE_LEGACY)
//            );
//        }
//    }
//
//
//    private String formatMessage(String text) {
//        // Thay thế Markdown cơ bản sang HTML
//        text = text.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>"); // In đậm
//        text = text.replaceAll("\\*(.*?)\\*", "• $1<br>"); // Gạch đầu dòng
//        text = text.replace("\n", "<br>");
//        return text; // KHÔNG .toString() nữa
//    }
//
//
//    @Override
//    public int getItemCount() {
//        return chatMessages.size();
//    }
//
//    static class UserViewHolder extends RecyclerView.ViewHolder {
//        TextView tvMessageUser;
//        UserViewHolder(@NonNull View itemView) {
//            super(itemView);
//            tvMessageUser = itemView.findViewById(R.id.tvMessageUser);
//        }
//    }
//
//    static class BotViewHolder extends RecyclerView.ViewHolder {
//        TextView tvMessageBot;
//        BotViewHolder(@NonNull View itemView) {
//            super(itemView);
//            tvMessageBot = itemView.findViewById(R.id.tvMessageBot);
//        }
//    }
//}



//
//package com.example.final_project.views.adapter;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.final_project.R;
//import com.example.final_project.models.entity.ChatMessage;
//
//import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//
//    private final List<ChatMessage> chatMessages;
//    private static final int VIEW_TYPE_USER = 1;
//    private static final int VIEW_TYPE_BOT = 2;
//
//    public ChatAdapter(List<ChatMessage> chatMessages) {
//        this.chatMessages = chatMessages;
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//        return chatMessages.get(position).isUser() ? VIEW_TYPE_USER : VIEW_TYPE_BOT;
//    }
//
//    @NonNull
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        if (viewType == VIEW_TYPE_USER) {
//            View view = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.item_chat_user, parent, false);
//            return new UserViewHolder(view);
//        } else {
//            View view = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.item_chat_bot, parent, false);
//            return new BotViewHolder(view);
//        }
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        ChatMessage message = chatMessages.get(position);
//
//        if (holder.getItemViewType() == VIEW_TYPE_USER) {
//            ((UserViewHolder) holder).tvMessageUser.setText(message.getMessage());
//        } else {
//            BotViewHolder botHolder = (BotViewHolder) holder;
//            String fullResponse = message.getMessage();
//
//            // Tách phần giới thiệu (nếu có) và phần công thức
//            String introText = "";
//            String recipesText = fullResponse;
//            int firstRecipeMarker = fullResponse.indexOf("## ");
//            if (firstRecipeMarker > 0) {
//                introText = fullResponse.substring(0, firstRecipeMarker).trim();
//                recipesText = fullResponse.substring(firstRecipeMarker);
//            } else if (firstRecipeMarker == 0) {
//                recipesText = fullResponse;
//            } else {
//                introText = fullResponse;
//                recipesText = "";
//            }
//
//            botHolder.tvBotIntro.setText(introText);
//            botHolder.tvBotIntro.setVisibility(introText.isEmpty() ? View.GONE : View.VISIBLE);
//            botHolder.llRecipeContainer.removeAllViews();
//
//            if (!recipesText.isEmpty()) {
//                // Tách các công thức bằng dấu "---"
//                String[] recipes = recipesText.split("---");
//
//                for (String recipeBlock : recipes) {
//                    String trimmedBlock = recipeBlock.trim();
//                    if (trimmedBlock.isEmpty()) continue;
//
//                    LayoutInflater inflater = LayoutInflater.from(botHolder.itemView.getContext());
//                    View recipeCardView = inflater.inflate(R.layout.item_recipe_card, botHolder.llRecipeContainer, false);
//
//                    TextView tvTitle = recipeCardView.findViewById(R.id.tvRecipeTitle);
//                    TextView tvIngredients = recipeCardView.findViewById(R.id.tvRecipeIngredients);
//                    TextView tvInstructions = recipeCardView.findViewById(R.id.tvRecipeInstructions);
//                    TextView tvTime = recipeCardView.findViewById(R.id.tvRecipeTime);
//
//                    // Trích xuất thông tin bằng các marker mới
//                    String title = extractSection(trimmedBlock, "##");
//                    String ingredients = extractSection(trimmedBlock, "@@nguyenlieu");
//                    String instructions = extractSection(trimmedBlock, "@@congthuc");
//                    String time = extractSection(trimmedBlock, "@@thoigian");
//
//                    tvTitle.setText(title);
//                    tvIngredients.setText(ingredients);
//                    tvInstructions.setText(instructions);
//                    tvTime.setText("Thời gian: " + time);
//
//                    botHolder.llRecipeContainer.addView(recipeCardView);
//                }
//            }
//        }
//    }
//
//    /**
//     * Hàm phụ trợ mới để trích xuất nội dung dựa trên các marker.
//     * Ví dụ: extractSection(text, "@@nguyenlieu")
//     */
//    private String extractSection(String text, String marker) {
//        try {
//            // Tạo pattern để tìm marker và lấy nội dung cho đến hết dòng hoặc đến marker tiếp theo
//            Pattern pattern = Pattern.compile(Pattern.quote(marker) + "\\s(.*?)(?=\\n##|\\n@@|$)", Pattern.DOTALL);
//            Matcher matcher = pattern.matcher(text);
//            if (matcher.find()) {
//                return matcher.group(1).trim();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return "Không có thông tin"; // Giá trị mặc định
//    }
//
//
//    @Override
//    public int getItemCount() {
//        return chatMessages.size();
//    }
//
//    static class UserViewHolder extends RecyclerView.ViewHolder {
//        TextView tvMessageUser;
//        UserViewHolder(@NonNull View itemView) {
//            super(itemView);
//            tvMessageUser = itemView.findViewById(R.id.tvMessageUser);
//        }
//    }
//
//    static class BotViewHolder extends RecyclerView.ViewHolder {
//        TextView tvBotIntro;
//        LinearLayout llRecipeContainer;
//        BotViewHolder(@NonNull View itemView) {
//            super(itemView);
//            tvBotIntro = itemView.findViewById(R.id.tvBotIntro);
//            llRecipeContainer = itemView.findViewById(R.id.llRecipeContainer);
//        }
//    }
//}


//------------------------------
//
//// File: views/adapter/ChatAdapter.java
//package com.example.final_project.views.adapter;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//import com.example.final_project.R;
//import com.example.final_project.models.entity.ChatMessage;
//import com.example.final_project.models.entity.RecipeData;
//
//import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//
//    public interface OnRecipeSaveListener {
//        void onSaveRecipeClicked(RecipeData recipe);
//    }
//
//    private final List<ChatMessage> chatMessages;
//    private static final int VIEW_TYPE_USER = 1;
//    private static final int VIEW_TYPE_BOT = 2;
//    private final OnRecipeSaveListener saveListener;
//
//    public ChatAdapter(List<ChatMessage> chatMessages, OnRecipeSaveListener listener) {
//        this.chatMessages = chatMessages;
//        this.saveListener = listener;
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//        return chatMessages.get(position).isUser() ? VIEW_TYPE_USER : VIEW_TYPE_BOT;
//    }
//
//    @NonNull
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        if (viewType == VIEW_TYPE_USER) {
//            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
//            return new UserViewHolder(view);
//        } else {
//            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_bot, parent, false);
//            return new BotViewHolder(view);
//        }
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        ChatMessage message = chatMessages.get(position);
//        if (holder.getItemViewType() == VIEW_TYPE_USER) {
//            ((UserViewHolder) holder).tvMessageUser.setText(message.getMessage());
//        } else {
//            BotViewHolder botHolder = (BotViewHolder) holder;
//            String fullResponse = message.getMessage();
//            String introText = "";
//            String recipesText = fullResponse;
//            int firstRecipeMarker = fullResponse.indexOf("## ");
//
//            if (firstRecipeMarker >= 0) {
//                introText = firstRecipeMarker > 0 ? fullResponse.substring(0, firstRecipeMarker).trim() : "";
//                recipesText = fullResponse.substring(firstRecipeMarker);
//            } else {
//                introText = fullResponse;
//                recipesText = "";
//            }
//
//            botHolder.tvBotIntro.setText(introText);
//            botHolder.tvBotIntro.setVisibility(introText.isEmpty() ? View.GONE : View.VISIBLE);
//            botHolder.llRecipeContainer.removeAllViews();
//
//            if (!recipesText.isEmpty()) {
//                String[] recipes = recipesText.split("---");
//                for (String recipeBlock : recipes) {
//                    String trimmedBlock = recipeBlock.trim();
//                    if (trimmedBlock.isEmpty()) continue;
//
//                    LayoutInflater inflater = LayoutInflater.from(botHolder.itemView.getContext());
//                    View recipeCardView = inflater.inflate(R.layout.item_recipe_card, botHolder.llRecipeContainer, false);
//                    TextView tvTitle = recipeCardView.findViewById(R.id.tvRecipeTitle);
//                    TextView tvIngredients = recipeCardView.findViewById(R.id.tvRecipeIngredients);
//                    TextView tvInstructions = recipeCardView.findViewById(R.id.tvRecipeInstructions);
//                    TextView tvTime = recipeCardView.findViewById(R.id.tvRecipeTime);
//                    Button btnSave = recipeCardView.findViewById(R.id.btnSaveRecipe);
//
//                    String title = extractSection(trimmedBlock, "##");
//                    String ingredients = extractSection(trimmedBlock, "@@nguyenlieu");
//                    String instructions = extractSection(trimmedBlock, "@@congthuc");
//                    String time = extractSection(trimmedBlock, "@@thoigian");
//                    String nutrition = extractSection(trimmedBlock, "@@dinhduong"); // <-- Lấy dinh dưỡng
//                    final RecipeData recipeData = new RecipeData(title, ingredients, instructions, time,nutrition);
//
//                    tvTitle.setText(recipeData.getTitle());
//                    tvIngredients.setText(recipeData.getIngredients());
//                    tvInstructions.setText(recipeData.getInstructions());
//                    tvTime.setText("Thời gian: " + recipeData.getTime());
//
//                    btnSave.setOnClickListener(v -> {
//                        if (saveListener != null) {
//                            saveListener.onSaveRecipeClicked(recipeData);
//                            btnSave.setText("Đã lưu");
//                            btnSave.setEnabled(false);
//                        }
//                    });
//
//                    botHolder.llRecipeContainer.addView(recipeCardView);
//                }
//            }
//        }
//    }
//
//    private String extractSection(String text, String marker) {
//        try {
//            Pattern pattern = Pattern.compile(Pattern.quote(marker) + "\\s(.*?)(?=\\n##|\\n@@|$)", Pattern.DOTALL);
//            Matcher matcher = pattern.matcher(text);
//            if (matcher.find()) {
//                return matcher.group(1).trim();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return "Không có thông tin";
//    }
//
//    @Override
//    public int getItemCount() {
//        return chatMessages.size();
//    }
//
//    // ===== LỖI CỦA BẠN LÀ Ở ĐÂY: CÁC CLASS NÀY BỊ TRỐNG =====
//    // Dưới đây là phiên bản đã được sửa lại đầy đủ.
//
//    static class UserViewHolder extends RecyclerView.ViewHolder {
//        TextView tvMessageUser;
//        UserViewHolder(@NonNull View itemView) {
//            super(itemView);
//            // Ánh xạ TextView cho tin nhắn của người dùng
//            tvMessageUser = itemView.findViewById(R.id.tvMessageUser);
//        }
//    }
//
//    static class BotViewHolder extends RecyclerView.ViewHolder {
//        TextView tvBotIntro;
//        LinearLayout llRecipeContainer;
//        BotViewHolder(@NonNull View itemView) {
//            super(itemView);
//            // Ánh xạ các view cho tin nhắn của bot
//            tvBotIntro = itemView.findViewById(R.id.tvBotIntro);
//            llRecipeContainer = itemView.findViewById(R.id.llRecipeContainer);
//        }
//    }
//}

package com.example.final_project.views.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView; // Xóa Button và LinearLayout vì không dùng ở đây nữa

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager; // Thêm import này
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.R;
import com.example.final_project.models.entity.ChatMessage;
import com.example.final_project.models.entity.RecipeData;

import java.util.ArrayList; // Thêm import này
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnRecipeSaveListener {
        void onSaveRecipeClicked(RecipeData recipe);
    }

    private final List<ChatMessage> chatMessages;
    private final OnRecipeSaveListener saveListener;

    // --- SỬA BƯỚC 1: Định nghĩa 3 loại View ---
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_BOT_TEXT = 2; // Tin nhắn text của Bot (ví dụ: "Đang suy nghĩ...")
    private static final int VIEW_TYPE_BOT_RECIPE = 3; // Tin nhắn chứa danh sách công thức

    public ChatAdapter(List<ChatMessage> chatMessages, OnRecipeSaveListener listener) {
        this.chatMessages = chatMessages;
        this.saveListener = listener;
    }

    // --- SỬA BƯỚC 2: Sửa logic getItemViewType ---
    @Override
    public int getItemViewType(int position) {
        ChatMessage message = chatMessages.get(position);
        if (message.isUser()) {
            return VIEW_TYPE_USER;
        } else {
            // Kiểm tra xem tin nhắn bot là text hay là công thức
            String botMessage = message.getMessage();
            // Dùng logic cũ của bạn để phân biệt
            if (botMessage.contains("##") && botMessage.contains("@@nguyenlieu")) {
                return VIEW_TYPE_BOT_RECIPE;
            } else {
                // Đây là tin nhắn text thường (Xin chào, Đang suy nghĩ,...)
                return VIEW_TYPE_BOT_TEXT;
            }
        }
    }

    // --- SỬA BƯỚC 3: Sửa logic onCreateViewHolder ---
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_USER) {
            View view = inflater.inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(view);

        } else if (viewType == VIEW_TYPE_BOT_TEXT) {
            // Sử dụng layout text mới (item_chat_bot_text.xml)
            View view = inflater.inflate(R.layout.item_chat_bot_text, parent, false);
            return new BotTextViewHolder(view);

        } else { // viewType == VIEW_TYPE_BOT_RECIPE
            // Sử dụng layout chứa RecyclerView (item_chat_bot.xml)
            View view = inflater.inflate(R.layout.item_chat_bot, parent, false);
            return new RecipeParentViewHolder(view); // Đổi tên ViewHolder
        }
    }

    // --- SỬA BƯỚC 4: Sửa logic onBindViewHolder ---
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);

        // Phân loại view và bind dữ liệu tương ứng
        int viewType = holder.getItemViewType();

        if (viewType == VIEW_TYPE_USER) {
            ((UserViewHolder) holder).tvMessageUser.setText(message.getMessage());

        } else if (viewType == VIEW_TYPE_BOT_TEXT) {
            ((BotTextViewHolder) holder).tvBotMessage.setText(message.getMessage());

        } else if (viewType == VIEW_TYPE_BOT_RECIPE) {
            // Gọi hàm bind cho ViewHolder công thức
            bindRecipeParentViewHolder((RecipeParentViewHolder) holder, message);
        }

        // --- SỬA BƯỚC 5: XÓA BỎ HOÀN TOÀN code "hack" padding ---
        // Không còn: if (position == chatMessages.size() - 1) ...
        // File activity_chat.xml đã xử lý việc này.
    }

    /**
     * Hàm mới: Chỉ bind dữ liệu cho ViewHolder chứa RecyclerView công thức
     */
    private void bindRecipeParentViewHolder(RecipeParentViewHolder holder, ChatMessage message) {
        String fullResponse = message.getMessage();
        String introText = "";
        String recipesText = fullResponse;

        // Cắt phần giới thiệu (giữ nguyên logic cũ của bạn)
        int firstRecipeMarker = fullResponse.indexOf("## ");
        if (firstRecipeMarker >= 0) {
            introText = firstRecipeMarker > 0 ? fullResponse.substring(0, firstRecipeMarker).trim() : "";
            recipesText = fullResponse.substring(firstRecipeMarker);
        } else {
            introText = fullResponse;
            recipesText = "";
        }

        // Set text giới thiệu
        holder.tvBotIntro.setText(introText);
        holder.tvBotIntro.setVisibility(introText.isEmpty() ? View.GONE : View.VISIBLE);

        // --- SỬA BƯỚC 6: Chuẩn bị dữ liệu cho Nested RecyclerView ---
        List<RecipeData> recipeList = new ArrayList<>();
        if (!recipesText.isEmpty()) {
            String[] recipes = recipesText.split("---");
            for (String recipeBlock : recipes) {
                String trimmedBlock = recipeBlock.trim();
                if (trimmedBlock.isEmpty()) continue;

                // Trích xuất dữ liệu (giữ nguyên logic cũ)
                String title = extractSection(trimmedBlock, "##");
                String ingredients = extractSection(trimmedBlock, "@@nguyenlieu");
                String instructions = extractSection(trimmedBlock, "@@congthuc");
                String time = extractSection(trimmedBlock, "@@thoigian");
                String nutrition = extractSection(trimmedBlock, "@@dinhduong");

                // Thêm vào List, thay vì addView
                recipeList.add(new RecipeData(title, ingredients, instructions, time, nutrition));
            }
        }

        // --- SỬA BƯỚC 7: Cập nhật cho Nested RecyclerView ---

        // 1. Kiểm tra nếu adapter của RecyclerView con chưa được tạo
        if (holder.recipeCardAdapter == null) {
            holder.recipeCardAdapter = new RecipeCardAdapter(recipeList, saveListener);
            holder.recyclerViewRecipes.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
            holder.recyclerViewRecipes.setAdapter(holder.recipeCardAdapter);
        } else {
            // 2. Nếu đã tồn tại (do tái sử dụng), chỉ cần cập nhật data
            holder.recipeCardAdapter.updateRecipes(recipeList);
        }
    }


    /**
     * Hàm trích xuất nội dung (không đổi)
     */
    private String extractSection(String text, String marker) {
        try {
            Pattern pattern = Pattern.compile(Pattern.quote(marker) + "\\s(.*?)(?=\\n##|\\n@@|$)", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Không có thông tin";
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    // --- ViewHolder người dùng (không đổi) ---
    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageUser;
        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageUser = itemView.findViewById(R.id.tvMessageUser);
        }
    }

    // --- SỬA BƯỚC 8: Thêm ViewHolder cho Bot Text ---
    static class BotTextViewHolder extends RecyclerView.ViewHolder {
        TextView tvBotMessage;
        BotTextViewHolder(@NonNull View itemView) {
            super(itemView);
            // ID này phải khớp với file item_chat_bot_text.xml
            tvBotMessage = itemView.findViewById(R.id.tvBotMessage);
        }
    }

    // --- SỬA BƯỚC 9: Đổi tên BotViewHolder thành RecipeParentViewHolder ---
    static class RecipeParentViewHolder extends RecyclerView.ViewHolder {
        TextView tvBotIntro;
        RecyclerView recyclerViewRecipes; // THAY THẾ LinearLayout
        RecipeCardAdapter recipeCardAdapter; // Thêm tham chiếu đến adapter con

        RecipeParentViewHolder(@NonNull View itemView) {
            super(itemView);
            // ID này phải khớp với file item_chat_bot.xml
            tvBotIntro = itemView.findViewById(R.id.tvBotIntro);
            recyclerViewRecipes = itemView.findViewById(R.id.recyclerViewRecipes); // ID của RecyclerView
        }
    }
}
