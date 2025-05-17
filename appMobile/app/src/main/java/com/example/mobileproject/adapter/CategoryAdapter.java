package com.example.mobileproject.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileproject.R;
import com.example.mobileproject.model.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final Context context;
    private final List<Category> categoryList;
    private final OnCategoryClickListener listener;
    private int selectedPosition = -1;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(Context context, List<Category> categoryList, OnCategoryClickListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.categoryName.setText(category.getName());

        // Thiết lập màu nền dựa trên trạng thái được chọn
        if (selectedPosition == position) {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.button_purple));
            holder.categoryName.setTextColor(ContextCompat.getColor(context, R.color.white));
        } else {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.card_dark));
            holder.categoryName.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
        }

        // Thiết lập sự kiện click
        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            // Cập nhật giao diện
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);

            // Gọi callback
            listener.onCategoryClick(category);
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView categoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.categoryCard);
            categoryName = itemView.findViewById(R.id.categoryName);
        }
    }
}