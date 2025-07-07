package com.example.ssutudy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TodoWithCategoryAdapter extends RecyclerView.Adapter<TodoWithCategoryAdapter.TodoViewHolder> {

    private List<TodoWithCategoryModel> todos;

    public TodoWithCategoryAdapter(List<TodoWithCategoryModel> todos) {
        this.todos = todos != null ? todos : new ArrayList<>();
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo_with_category, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        TodoWithCategoryModel todo = todos.get(position);

        holder.categoryName.setText(todo.getCategoryName());
        holder.taskText.setText(todo.getTodoName());
    }

    @Override
    public int getItemCount() {
        return todos != null ? todos.size() : 0;
    }

    public void updateTodos(List<TodoWithCategoryModel> newTodos) {
        this.todos = newTodos != null ? newTodos : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class TodoViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        TextView taskText;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.categoryName);
            taskText = itemView.findViewById(R.id.taskText);
        }
    }
}