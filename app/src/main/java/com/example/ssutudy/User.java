package com.example.ssutudy;

public class User {
    private String name;
    private String studentNum;
    private String alias;
    private String image_url;
    private int totalStudyTime;
    private String state;
    private int ranking;

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }

    public User(){}

    public User(String name, String studentNum, String alias, String image_url, int totalStudyTime, String state, int ranking) {
        this.name = name;
        this.studentNum = studentNum;
        this.alias = alias;
        this.image_url = image_url;
        this.totalStudyTime = totalStudyTime;
        this.state = state;
        this.ranking = ranking;
    }

    public User(String name, String studentNum, String alias, String image_url, int totalStudyTime, String state) {
        this.name = name;
        this.studentNum = studentNum;
        this.alias = alias;
        this.image_url = image_url;
        this.totalStudyTime = totalStudyTime;
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStudentNum() {
        return studentNum;
    }

    public void setStudentNum(String studentNum) {
        this.studentNum = studentNum;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public int getTotalStudyTime() {
        return totalStudyTime;
    }

    public void setTotalStudyTime(int totalStudyTime) {
        this.totalStudyTime = totalStudyTime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
