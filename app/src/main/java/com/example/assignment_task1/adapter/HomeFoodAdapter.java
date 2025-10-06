package com.example.assignment_task1.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.assignment_task1.R;
import java.util.List;

public class HomeFoodAdapter extends RecyclerView.Adapter<HomeFoodAdapter.FoodViewHolder> {
    private final Context context;
    private final List<FoodItem> foodList;

    public HomeFoodAdapter(Context context, List<FoodItem> foodList) {
        this.context = context;
        this.foodList = foodList;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_home_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodItem item = foodList.get(position);
        holder.tvFoodName.setText(item.getName());
        // Load image from assets/food_images
        try {
            holder.ivFoodImage.setImageBitmap(
                android.graphics.BitmapFactory.decodeStream(
                    context.getAssets().open("food_images/" + item.getImageName())
                )
            );
        } catch (Exception e) {
            holder.ivFoodImage.setImageResource(R.drawable.ic_food_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoodImage;
        TextView tvFoodName;
        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoodImage = itemView.findViewById(R.id.ivFoodImage);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
        }
    }

    public static class FoodItem {
        private final String name;
        private final String imageName;
        public FoodItem(String name, String imageName) {
            this.name = name;
            this.imageName = imageName;
        }
        public String getName() { return name; }
        public String getImageName() { return imageName; }
    }
}

