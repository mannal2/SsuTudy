package com.example.ssutudy;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<CategoryModel> categories;
    private final OnCategoryActionListener listener;
    private final RecyclerView.RecycledViewPool viewPool;

    public CategoryAdapter(List<CategoryModel> categories, OnCategoryActionListener listener) {
        this.categories = categories != null ? categories : new ArrayList<>();
        this.listener = listener;
        this.viewPool = new RecyclerView.RecycledViewPool();
    }

    public interface OnCategoryActionListener {
        void onAddTodoDialog(String categoryId);
        void onAddTodo(String categoryId, String todoName);
        void onTodoChecked(String categoryId, String todoId, boolean isChecked);
        void onTodoEdit(String categoryId, String todoId);
        void onTodoDelete(String categoryId, String todoId, String todoName);
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryModel category = categories.get(position);
        holder.categoryName.setText(category.getCategoryName());

        Log.d("CategoryTypeDebug", "Category type is: " + category.getType());
        if ("group".equals(category.getType())) {
            holder.categoryIcon.setImageResource(R.drawable.ic_group_category);
        } else {
            holder.categoryIcon.setImageResource(R.drawable.ic_custom_category);
        }

        TodoAdapter todoAdapter = new TodoAdapter(
                category.getTodos(),
                new TodoAdapter.OnTodoActionListener() {
                    @Override
                    public void onTodoChecked(String categoryId, String todoId, boolean isChecked) {
                        listener.onTodoChecked(category.getId(), todoId, isChecked);
                    }

                    @Override
                    public void onTodoEdit(String categoryId, String todoId) {
                        listener.onTodoEdit(category.getId(), todoId);
                    }

                    @Override
                    public void onTodoDelete(String categoryId, String todoId, String todoName) {
                        listener.onTodoDelete(category.getId(), todoId, todoName);
                    }
                }
        );

        holder.todoRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.todoRecyclerView.setAdapter(todoAdapter);
        holder.todoRecyclerView.setRecycledViewPool(viewPool);

        holder.addTodoButton.setOnClickListener(v -> listener.onAddTodoDialog(category.getId()));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void updateCategories(List<CategoryModel> newCategories) {
        this.categories = newCategories != null ? newCategories : new ArrayList<>();
        notifyDataSetChanged();
    }

    public List<CategoryModel> getCategories() {
        return categories;
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        RecyclerView todoRecyclerView;
        ImageButton addTodoButton;
        ImageView categoryIcon;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.category_name);
            todoRecyclerView = itemView.findViewById(R.id.todo_recycler_view);
            addTodoButton = itemView.findViewById(R.id.add_todo_button);
            categoryIcon = itemView.findViewById(R.id.ic_category);
        }
    }
}