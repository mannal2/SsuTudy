package com.example.ssutudy;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CourseHolder2 extends RecyclerView.ViewHolder {
    private final TextView courseRankTimeField;
    private final TextView courseRankingField;
    public final ImageView courseRankImageField;
    private final TextView courseRankAliasField;

    public CourseHolder2(@NonNull View itemView) {
        super(itemView);
        this.courseRankTimeField = itemView.findViewById(R.id.course_rank_time);
        this.courseRankingField = itemView.findViewById(R.id.course_rank_ranking);
        this.courseRankImageField = itemView.findViewById(R.id.course_rank_image);
        this.courseRankAliasField = itemView.findViewById(R.id.course_rank_alias);
    }

    public void bind(@NonNull Course course){
        String t = course.getCourseStudyTime()+" Min";
        courseRankTimeField.setText(t);
    }

    public void setRanking(int n){
        String t = "#"+n;
        courseRankingField.setText(t);
    }

    public void setAlias(String n){
        courseRankAliasField.setText(n);
    }
}
