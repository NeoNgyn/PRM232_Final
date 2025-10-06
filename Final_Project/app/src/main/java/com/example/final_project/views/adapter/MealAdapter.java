package com.example.final_project.views.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.views.activity.MealDetailActivity;
import com.example.final_project.R;
import com.example.final_project.models.entity.Meal;

import java.util.List;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {
    private List<Meal> mealList;

    public MealAdapter(List<Meal> mealList) {
        this.mealList = mealList;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal_card, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        // Lấy đúng món ăn ở vị trí hiện tại
        Meal meal = mealList.get(position);

        // Gán dữ liệu lên các view
        holder.mealName.setText(meal.getName());
        holder.mealImage.setImageResource(meal.getImageResource());

        // Ẩn hoặc hiện tag "Pro"
        if (meal.isPro()) {
            holder.proTag.setVisibility(View.VISIBLE);
        } else {
            holder.proTag.setVisibility(View.GONE);
        }

        // === ĐÂY LÀ PHẦN SỬA LẠI QUAN TRỌNG NHẤT ===
        // Đặt listener ở đây để có thể truy cập biến `meal` ở trên
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent = new Intent(context, MealDetailActivity.class);

                // Gửi dữ liệu của món ăn được click đi
                intent.putExtra("MEAL_NAME", meal.getName());
                intent.putExtra("MEAL_IMAGE", meal.getImageResource());

                // Khởi chạy Activity mới
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }

    // Lớp ViewHolder bây giờ chỉ làm nhiệm vụ tìm và giữ các view
    public static class MealViewHolder extends RecyclerView.ViewHolder {
        ImageView mealImage;
        TextView mealName;
        TextView proTag;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            mealImage = itemView.findViewById(R.id.iv_meal_image);
            mealName = itemView.findViewById(R.id.tv_meal_name);
            proTag = itemView.findViewById(R.id.tv_pro_tag);
            // Đã xóa OnClickListener khỏi đây
        }
    }
}
