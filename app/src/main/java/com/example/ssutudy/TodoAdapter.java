package com.example.ssutudy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {
    private List<TodoModel> todos;
    private final OnTodoActionListener listener;
    private String categoryId;

    public TodoAdapter(List<TodoModel> todos, OnTodoActionListener listener, String categoryId) {
        this.todos = todos != null ? todos : new ArrayList<>();
        this.listener = listener;
        this.categoryId = categoryId;
    }

    public interface OnTodoActionListener {
        void onTodoChecked(String categoryId, String todoId, boolean isChecked);
        void onTodoEdit(String categoryId, String todoId);
        void onTodoDelete(String categoryId, String todoId, String todoName);
    }

    public TodoAdapter(List<TodoModel> todos, OnTodoActionListener listener) {
        this.todos = todos != null ? todos : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        TodoModel todo = todos.get(position);
        holder.todoName.setText(todo.getTodoName());
        holder.todoCheckbox.setChecked(todo.isCompleted());

        // 체크박스 변경 이벤트
        holder.todoCheckbox.setOnCheckedChangeListener((buttonView, isChecked) ->
                listener.onTodoChecked(todo.getId(), todo.getId(), isChecked));

        // 수정 및 삭제 버튼
        holder.editButton.setOnClickListener(v -> listener.onTodoEdit(todo.getId(), todo.getId()));

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTodoDelete(categoryId, todo.getId(), todo.getTodoName());
            }
        });    }

    @Override
    public int getItemCount() {
        return todos.size();
    }

    public void updateTodos(List<TodoModel> newTodos) {
        this.todos = newTodos != null ? newTodos : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class TodoViewHolder extends RecyclerView.ViewHolder {
        TextView todoName;
        CheckBox todoCheckbox;
        ImageButton editButton;
        ImageButton deleteButton;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            todoName = itemView.findViewById(R.id.todo_name);
            todoCheckbox = itemView.findViewById(R.id.todo_checkbox);
            editButton = itemView.findViewById(R.id.todo_edit_button);
            deleteButton = itemView.findViewById(R.id.todo_delete_button);
        }
    }
}