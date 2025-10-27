package com.example.final_project.views.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.PopupMenu;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.final_project.R;
import java.util.List;

public class HomeMenuAdapter extends RecyclerView.Adapter<HomeMenuAdapter.MenuViewHolder> {
    private final Context context;
    private final List<MenuItem> menuList;
    private final OnMenuActionListener actionListener;

    public interface OnMenuActionListener {
        void onEditMenu(MenuItem menuItem, int position);
        void onDeleteMenu(MenuItem menuItem, int position);
    }

    public HomeMenuAdapter(Context context, List<MenuItem> menuList) {
        this.context = context;
        this.menuList = menuList;
        this.actionListener = null;
    }

    public HomeMenuAdapter(Context context, List<MenuItem> menuList, OnMenuActionListener actionListener) {
        this.context = context;
        this.menuList = menuList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_home_menu, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        MenuItem item = menuList.get(position);
        holder.tvMenuFoodName.setText(item.getName());
        holder.tvMenuFoodDesc.setText(item.getDesc());

        // Load image from URL using Glide
        String imageUrl = item.getImageName();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_food_placeholder)
                    .error(R.drawable.ic_food_placeholder)
                    .into(holder.ivMenuFoodImage);
        } else {
            holder.ivMenuFoodImage.setImageResource(R.drawable.ic_food_placeholder);
        }

        // Sửa: Khi bấm nút Edit, chuyển sang CreateRecipeActivity và truyền dữ liệu
        holder.btnEditMenu.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, com.example.final_project.views.activity.CreateRecipeActivity.class);
            // Send both keys to be compatible with different receivers
            intent.putExtra("menuId", item.getMenuId());
            intent.putExtra("menu_id", item.getMenuId());
            intent.putExtra("name", item.getName());
            intent.putExtra("description", item.getDesc());
            intent.putExtra("imageUrl", item.getImageName());
            intent.putExtra("fromDate", item.getFromDate());
            intent.putExtra("toDate", item.getToDate());
            intent.putExtra("note", ""); // Nếu có trường note thì truyền, nếu không thì để rỗng
            context.startActivity(intent);
        });

        // Item click listener to open MenuDetailActivity (show menu details with recipes)
        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, com.example.final_project.views.activity.MenuDetailActivity.class);
            intent.putExtra("menu_id", item.getMenuId());
            intent.putExtra("menu_name", item.getName());
            intent.putExtra("menu_description", item.getDesc());
            intent.putExtra("menu_image_url", item.getImageName());
            intent.putExtra("from_date", item.getFromDate());
            intent.putExtra("to_date", item.getToDate());
            context.startActivity(intent);
        });

        // Set click listeners for edit and delete buttons if action listener is provided
        if (actionListener != null) {
            holder.btnEditMenu.setOnClickListener(v -> actionListener.onEditMenu(item, position));
            holder.btnDeleteMenu.setOnClickListener(v -> actionListener.onDeleteMenu(item, position));
        }
    }

    private void showContextMenu(View view, MenuItem item, int position) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.menu_context, popup.getMenu());
        popup.setOnMenuItemClickListener(menuItem -> {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.action_edit) {
                actionListener.onEditMenu(item, position);
                return true;
            } else if (itemId == R.id.action_delete) {
                actionListener.onDeleteMenu(item, position);
                return true;
            }
            return false;
        });
        popup.show();
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    public static class MenuViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMenuFoodImage;
        TextView tvMenuFoodName, tvMenuFoodDesc;
        Button btnEditMenu, btnDeleteMenu;
        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMenuFoodImage = itemView.findViewById(R.id.ivMenuFoodImage);
            tvMenuFoodName = itemView.findViewById(R.id.tvMenuFoodName);
            tvMenuFoodDesc = itemView.findViewById(R.id.tvMenuFoodDesc);
            btnEditMenu = itemView.findViewById(R.id.btnEditMenu);
            btnDeleteMenu = itemView.findViewById(R.id.btnDeleteMenu);
        }
    }

    public static class MenuItem {
        private final String menuId;
        private final String name;
        private final String imageName;
        private final String desc;
        private final String imageUrl;
        private final String description;
        private final String fromDate;
        private final String toDate;

        public MenuItem(String menuId, String name, String imageUrl, String description, String fromDate, String toDate) {
            this.menuId = menuId;
            this.name = name;
            this.imageName = imageUrl; // For backward compatibility with existing code
            this.desc = description; // For backward compatibility with existing code
            this.imageUrl = imageUrl;
            this.description = description;
            this.fromDate = fromDate;
            this.toDate = toDate;
        }

        // Constructor overload for backward compatibility with mock data
        public MenuItem(String name, String imageName, String desc) {
            this("", name, imageName, desc, "", "");
        }

        public String getMenuId() { return menuId; }
        public String getName() { return name; }
        public String getImageName() { return imageName; }
        public String getDesc() { return desc; }
        public String getImageUrl() { return imageUrl; }
        public String getDescription() { return description; }
        public String getFromDate() { return fromDate; }
        public String getToDate() { return toDate; }
    }
}
