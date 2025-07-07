package com.example.ssutudy;

public class TodoModel {
    private String id; // 할 일의 ID (Firebase에서 자동 생성된 문서 ID)
    private String todoName; // 할 일 이름
    private boolean completed; // 할 일 완료 여부

    // 기본 생성자 (Firebase에서 객체 매핑을 위해 필요)
    public TodoModel() {
    }

    public TodoModel(String id, String todoName, boolean completed) {
        this.id = id;
        this.todoName = todoName;
        this.completed = completed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTodoName() {
        return todoName;
    }

    public void setTodoName(String todoName) {
        this.todoName = todoName;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}