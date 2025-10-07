package com.example.final_project.views.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.final_project.R;
import java.util.List;

public class FeaturedFoodAdapter extends RecyclerView.Adapter<FeaturedFoodAdapter.FeaturedViewHolder> {

    private Context context;
    private List<FeaturedFood> featuredFoodList;

    public FeaturedFoodAdapter(Context context, List<FeaturedFood> featuredFoodList) {
        this.context = context;
        this.featuredFoodList = featuredFoodList;
    }

    @NonNull
    @Override
    public FeaturedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_featured_food, parent, false);
        return new FeaturedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedViewHolder holder, int position) {
        FeaturedFood featuredFood = featuredFoodList.get(position);

        holder.tvFeaturedName.setText(featuredFood.getName());

        // Load ảnh - có thể sử dụng Glide hoặc Picasso để load từ assets
        // Tạm thời dùng placeholder
        holder.ivFeaturedImage.setImageResource(R.drawable.ic_food_placeholder);

        // Xử lý click
        holder.itemView.setOnClickListener(v -> {
            // Handle click event - mở chi tiết món ăn
        });
    }

    @Override
    public int getItemCount() {
        return featuredFoodList != null ? featuredFoodList.size() : 0;
    }

    public static class FeaturedViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFeaturedImage;
        TextView tvFeaturedName;

        public FeaturedViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFeaturedImage = itemView.findViewById(R.id.ivFeaturedImage);
            tvFeaturedName = itemView.findViewById(R.id.tvFeaturedName);
        }
    }

    // Model class cho FeaturedFood
    public static class FeaturedFood {
        private String name;
        private String imageName;

        public FeaturedFood(String name, String imageName) {
            this.name = name;
            this.imageName = imageName;
        }

        public String getName() {
            return name;
        }

        public String getImageName() {
            return imageName;
        }
    }
}
