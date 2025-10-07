package com.example.final_project.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.final_project.R;
import java.util.List;

public class HomeMenuAdapter extends RecyclerView.Adapter<HomeMenuAdapter.MenuViewHolder> {
    private final Context context;
    private final List<MenuItem> menuList;

    public HomeMenuAdapter(Context context, List<MenuItem> menuList) {
        this.context = context;
        this.menuList = menuList;
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
        try {
            holder.ivMenuFoodImage.setImageBitmap(
                BitmapFactory.decodeStream(
                    context.getAssets().open("food_images/" + item.getImageName())
                )
            );
        } catch (Exception e) {
            holder.ivMenuFoodImage.setImageResource(R.drawable.ic_food_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    public static class MenuViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMenuFoodImage;
        TextView tvMenuFoodName, tvMenuFoodDesc;
        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMenuFoodImage = itemView.findViewById(R.id.ivMenuFoodImage);
            tvMenuFoodName = itemView.findViewById(R.id.tvMenuFoodName);
            tvMenuFoodDesc = itemView.findViewById(R.id.tvMenuFoodDesc);
        }
    }

    public static class MenuItem {
        private final String name;
        private final String imageName;
        private final String desc;
        public MenuItem(String name, String imageName, String desc) {
            this.name = name;
            this.imageName = imageName;
            this.desc = desc;
        }
        public String getName() { return name; }
        public String getImageName() { return imageName; }
        public String getDesc() { return desc; }
    }
}

