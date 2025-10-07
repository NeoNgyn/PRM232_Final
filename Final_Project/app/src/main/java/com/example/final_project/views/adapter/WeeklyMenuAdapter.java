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
import com.example.final_project.models.entity.Recipe;
import java.util.List;

public class WeeklyMenuAdapter extends RecyclerView.Adapter<WeeklyMenuAdapter.WeeklyViewHolder> {

    private Context context;
    private List<Recipe> dishList;

    public WeeklyMenuAdapter(Context context, List<Recipe> dishList) {
        this.context = context;
        this.dishList = dishList;
    }

    @NonNull
    @Override
    public WeeklyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_weekly_dish, parent, false);
        return new WeeklyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeeklyViewHolder holder, int position) {
        Recipe dish = dishList.get(position);

        holder.tvWeeklyDishName.setText(dish.getName());
        holder.tvWeeklyDishDesc.setText(dish.getDescription());

        // Load ảnh - tạm thời dùng placeholder
        holder.ivWeeklyDishImage.setImageResource(R.drawable.ic_food_placeholder);

        // Xử lý click
        holder.itemView.setOnClickListener(v -> {
            // Handle click event - mở chi tiết món ăn
        });
    }

    @Override
    public int getItemCount() {
        return dishList != null ? dishList.size() : 0;
    }

    public static class WeeklyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivWeeklyDishImage;
        TextView tvWeeklyDishName;
        TextView tvWeeklyDishDesc;

        public WeeklyViewHolder(@NonNull View itemView) {
            super(itemView);
            ivWeeklyDishImage = itemView.findViewById(R.id.ivWeeklyDishImage);
            tvWeeklyDishName = itemView.findViewById(R.id.tvWeeklyDishName);
            tvWeeklyDishDesc = itemView.findViewById(R.id.tvWeeklyDishDesc);
        }
    }
}
