package com.example.ssutudy;

import java.util.List;

public class Course {
    private String courseName;
    private String courseNum;
    private int courseStudyTime;
    private List<String> courseTime;
    private String studentNum;

    public Course(){}

    public Course(String courseName, String courseNum, int courseStudyTime, List<String> courseTime, String studentNum) {
        this.courseName = courseName;
        this.courseNum = courseNum;
        this.courseStudyTime = courseStudyTime;
        this.courseTime = courseTime;
        this.studentNum = studentNum;
    }

    public String getStudentNum() {
        return studentNum;
    }

    public void setStudentNum(String studentNum) {
        this.studentNum = studentNum;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseNum() {
        return courseNum;
    }

    public void setCourseNum(String courseNum) {
        this.courseNum = courseNum;
    }

    public int getCourseStudyTime() {
        return courseStudyTime;
    }

    public void setCourseStudyTime(int courseStudyTime) {
        this.courseStudyTime = courseStudyTime;
    }
}
