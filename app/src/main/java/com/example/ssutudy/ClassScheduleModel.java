package com.example.ssutudy;

public class ClassScheduleModel {
    private String id; // Firestore 문서 ID
    private String className;
    private String startTime;
    private String endTime;
    private String classPlace;

    // 기본 생성자 (Firestore에서 데이터 매핑 시 필요)
    public ClassScheduleModel() {}

    public ClassScheduleModel(String id, String className, String classPlace, String startTime, String endTime) {
        this.id = id;
        this.className = className;
        this.classPlace = classPlace;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getter and Setter

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getClassPlace() {
        return classPlace == null ? "" : classPlace;
    }
    public void setClassPlace(String classPlace) {
        this.classPlace = classPlace;
    }
}