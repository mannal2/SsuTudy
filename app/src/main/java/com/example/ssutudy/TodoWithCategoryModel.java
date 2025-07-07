package com.example.ssutudy;

public class TodoWithCategoryModel {
    private String categoryName;
    private String todoName;

    public TodoWithCategoryModel() { }

    public TodoWithCategoryModel(String categoryName, String todoName) {
        this.categoryName = categoryName;
        this.todoName = todoName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getTodoName() {
        return todoName;
    }

    public void setTodoName(String todoName) {
        this.todoName = todoName;
    }
}