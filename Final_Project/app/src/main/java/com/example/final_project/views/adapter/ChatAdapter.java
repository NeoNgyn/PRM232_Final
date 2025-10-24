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




// File: views/adapter/ChatAdapter.java
package com.example.final_project.views.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.final_project.R;
import com.example.final_project.models.entity.ChatMessage;
import com.example.final_project.models.entity.RecipeData;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnRecipeSaveListener {
        void onSaveRecipeClicked(RecipeData recipe);
    }

    private final List<ChatMessage> chatMessages;
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_BOT = 2;
    private final OnRecipeSaveListener saveListener;

    public ChatAdapter(List<ChatMessage> chatMessages, OnRecipeSaveListener listener) {
        this.chatMessages = chatMessages;
        this.saveListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return chatMessages.get(position).isUser() ? VIEW_TYPE_USER : VIEW_TYPE_BOT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_bot, parent, false);
            return new BotViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_USER) {
            ((UserViewHolder) holder).tvMessageUser.setText(message.getMessage());
        } else {
            BotViewHolder botHolder = (BotViewHolder) holder;
            String fullResponse = message.getMessage();
            String introText = "";
            String recipesText = fullResponse;
            int firstRecipeMarker = fullResponse.indexOf("## ");

            if (firstRecipeMarker >= 0) {
                introText = firstRecipeMarker > 0 ? fullResponse.substring(0, firstRecipeMarker).trim() : "";
                recipesText = fullResponse.substring(firstRecipeMarker);
            } else {
                introText = fullResponse;
                recipesText = "";
            }

            botHolder.tvBotIntro.setText(introText);
            botHolder.tvBotIntro.setVisibility(introText.isEmpty() ? View.GONE : View.VISIBLE);
            botHolder.llRecipeContainer.removeAllViews();

            if (!recipesText.isEmpty()) {
                String[] recipes = recipesText.split("---");
                for (String recipeBlock : recipes) {
                    String trimmedBlock = recipeBlock.trim();
                    if (trimmedBlock.isEmpty()) continue;

                    LayoutInflater inflater = LayoutInflater.from(botHolder.itemView.getContext());
                    View recipeCardView = inflater.inflate(R.layout.item_recipe_card, botHolder.llRecipeContainer, false);
                    TextView tvTitle = recipeCardView.findViewById(R.id.tvRecipeTitle);
                    TextView tvIngredients = recipeCardView.findViewById(R.id.tvRecipeIngredients);
                    TextView tvInstructions = recipeCardView.findViewById(R.id.tvRecipeInstructions);
                    TextView tvTime = recipeCardView.findViewById(R.id.tvRecipeTime);
                    Button btnSave = recipeCardView.findViewById(R.id.btnSaveRecipe);

                    String title = extractSection(trimmedBlock, "##");
                    String ingredients = extractSection(trimmedBlock, "@@nguyenlieu");
                    String instructions = extractSection(trimmedBlock, "@@congthuc");
                    String time = extractSection(trimmedBlock, "@@thoigian");
                    String nutrition = extractSection(trimmedBlock, "@@dinhduong"); // <-- Lấy dinh dưỡng
                    final RecipeData recipeData = new RecipeData(title, ingredients, instructions, time,nutrition);

                    tvTitle.setText(recipeData.getTitle());
                    tvIngredients.setText(recipeData.getIngredients());
                    tvInstructions.setText(recipeData.getInstructions());
                    tvTime.setText("Thời gian: " + recipeData.getTime());

                    btnSave.setOnClickListener(v -> {
                        if (saveListener != null) {
                            saveListener.onSaveRecipeClicked(recipeData);
                            btnSave.setText("Đã lưu");
                            btnSave.setEnabled(false);
                        }
                    });

                    botHolder.llRecipeContainer.addView(recipeCardView);
                }
            }
        }
    }

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

    // ===== LỖI CỦA BẠN LÀ Ở ĐÂY: CÁC CLASS NÀY BỊ TRỐNG =====
    // Dưới đây là phiên bản đã được sửa lại đầy đủ.

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageUser;
        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ TextView cho tin nhắn của người dùng
            tvMessageUser = itemView.findViewById(R.id.tvMessageUser);
        }
    }

    static class BotViewHolder extends RecyclerView.ViewHolder {
        TextView tvBotIntro;
        LinearLayout llRecipeContainer;
        BotViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các view cho tin nhắn của bot
            tvBotIntro = itemView.findViewById(R.id.tvBotIntro);
            llRecipeContainer = itemView.findViewById(R.id.llRecipeContainer);
        }
    }
}