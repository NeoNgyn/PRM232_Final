package com.example.final_project.views.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.final_project.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.final_project.models.entity.FoodItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.FoodViewHolder> {
    public interface OnFoodActionListener {
        void onEdit(FoodItem food, int position);
        void onDelete(FoodItem food, int position);
    }

    private List<FoodItem> foodList;
    private Context context;
    private OnFoodActionListener listener;

    public InventoryAdapter(Context context, List<FoodItem> foodList, OnFoodActionListener listener) {
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
        final FoodItem food = foodList.get(position);

        if (food == null) {
            // Defensive defaults to avoid crashes
            holder.tvFoodName.setText("--");
            holder.tvQuantity.setText(context.getString(R.string.food_quantity, 0));
            holder.tvExpiry.setText(context.getString(R.string.food_expiry, "--"));
            holder.imgFood.setImageResource(R.drawable.ic_food_placeholder);
            return;
        }

        holder.tvFoodName.setText(food.getFoodName() == null ? "--" : food.getFoodName());
        holder.tvQuantity.setText(context.getString(R.string.food_quantity, food.getQuantity()));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        if (food.getExpiryDate() != null) {
            holder.tvExpiry.setText(context.getString(R.string.food_expiry, sdf.format(food.getExpiryDate())));
        } else {
            holder.tvExpiry.setText(context.getString(R.string.food_expiry, "--"));
        }

        String image = food.getImageUrl();
        if (image != null && (image.startsWith("http://") || image.startsWith("https://"))) {
            Glide.with(context)
                    .load(image)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .placeholder(R.drawable.ic_food_placeholder)
                    .into(holder.imgFood);
        } else {
            int resId = context.getResources().getIdentifier(image == null ? "" : image, "drawable", context.getPackageName());
            if (resId != 0) holder.imgFood.setImageResource(resId);
            else holder.imgFood.setImageResource(R.drawable.ic_food_placeholder);
        }

        if (food.getExpiryDate() != null) {
            long daysLeft = (food.getExpiryDate().getTime() - new Date().getTime()) / (1000L * 60 * 60 * 24);
            if (daysLeft <= 2) {
                holder.tvExpiry.setTextColor(Color.RED);
            } else if (daysLeft <= 5) {
                holder.tvExpiry.setTextColor(Color.parseColor("#FFA500")); // Orange
            } else {
                holder.tvExpiry.setTextColor(Color.parseColor("#228B22")); // Green
            }
        } else {
            holder.tvExpiry.setTextColor(Color.parseColor("#666666")); // Default gray
        }

        if (listener != null) {
            holder.btnEdit.setOnClickListener(v -> listener.onEdit(food, position));
            holder.btnDelete.setOnClickListener(v -> listener.onDelete(food, position));
        } else {
            holder.btnEdit.setOnClickListener(null);
            holder.btnDelete.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return foodList == null ? 0 : foodList.size();
    }

    public void updateList(List<FoodItem> newList) {
        this.foodList = newList;
        notifyDataSetChanged();
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFood;
        TextView tvFoodName, tvQuantity, tvExpiry;
        Button btnEdit, btnDelete;
        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.imgFood);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvExpiry = itemView.findViewById(R.id.tvExpiry);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
