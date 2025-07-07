package com.example.ssutudy;

public class ScheduleModel {
    private String id; // Firestore 문서 ID
    private String title;
    private String place;
    private String time;

    // 기본 생성자 (Firebase Firestore에서 필요)
    public ScheduleModel() {
    }

    // 모든 필드를 초기화하는 생성자 (필요에 따라 사용)
    public ScheduleModel(String id, String title, String place, String time) {
        this.id = id;
        this.title = title;
        this.place = place;
        this.time = time;
    }

    // Getter와 Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}