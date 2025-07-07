package com.example.ssutudy;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CourseHolder extends RecyclerView.ViewHolder {
    private final TextView courseNumField;
    private final TextView courseNameField;
    private final TextView courseStudyTimeField;

    public CourseHolder(@NonNull View itemView) {
        super(itemView);
        courseNumField = itemView.findViewById(R.id.course_number);
        courseNameField = itemView.findViewById(R.id.course_name);
        courseStudyTimeField = itemView.findViewById(R.id.course_studytime);
    }

    public void bind(@NonNull Course course){
        String t = course.getCourseStudyTime()+" Min";
        courseStudyTimeField.setText(t);
        courseNumField.setText(course.getCourseNum());
        courseNameField.setText(course.getCourseName());
    }
}
