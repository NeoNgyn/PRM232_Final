package com.example.final_project.views.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.final_project.R;
import com.example.final_project.model.Food;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.FoodViewHolder> {
    public interface OnFoodActionListener {
        void onEdit(Food food, int position);
        void onDelete(Food food, int position);
    }

    private List<Food> foodList;
    private Context context;
    private OnFoodActionListener listener;

    public InventoryAdapter(Context context, List<Food> foodList, OnFoodActionListener listener) {
        this.context = context;
        this.foodList = foodList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        Food food = foodList.get(position);
        holder.tvFoodName.setText(food.getName());
        holder.tvQuantity.setText(context.getString(R.string.food_quantity, food.getQuantity()));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.tvExpiry.setText(context.getString(R.string.food_expiry, sdf.format(food.getExpiryDate())));
        holder.tvNote.setText(food.getNote());
        // Load image from drawable resource name
        int resId = context.getResources().getIdentifier(food.getImageResourceName(), "drawable", context.getPackageName());
        if (resId != 0) {
            holder.imgFood.setImageResource(resId);
        } else {
            holder.imgFood.setImageResource(R.drawable.ic_food_placeholder);
        }
        // Cảnh báo màu
        long daysLeft = (food.getExpiryDate().getTime() - new Date().getTime()) / (1000 * 60 * 60 * 24);
        if (daysLeft <= 2) {
            holder.tvExpiry.setTextColor(Color.RED);
        } else if (daysLeft <= 5) {
            holder.tvExpiry.setTextColor(Color.parseColor("#FFA500")); // vàng cam
        } else {
            holder.tvExpiry.setTextColor(Color.parseColor("#228B22")); // xanh lá
        }
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(food, position));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(food, position));
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public void updateList(List<Food> newList) {
        this.foodList = newList;
        notifyDataSetChanged();
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFood;
        TextView tvFoodName, tvQuantity, tvExpiry, tvNote;
        ImageButton btnEdit, btnDelete;
        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.imgFood);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvExpiry = itemView.findViewById(R.id.tvExpiry);
            tvNote = itemView.findViewById(R.id.tvNote);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
