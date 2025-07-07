package com.example.ssutudy;

import java.util.List;

public class CategoryModel {
    private String id;
    private String categoryName;
    private List<TodoModel> todos;
    private String type;

    public CategoryModel() { }

    public CategoryModel(String id, String categoryName, List<TodoModel> todos, String type) {
        this.id = id;
        this.categoryName = categoryName;
        this.todos = todos;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public List<TodoModel> getTodos() {
        return todos;
    }

    public void setTodos(List<TodoModel> todos) {
        this.todos = todos;
    }


    public String getType() { return type;}

    public void setType(String type) { this.type = type; }
}