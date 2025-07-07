package com.example.ssutudy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ClassScheduleAdapter extends RecyclerView.Adapter<ClassScheduleAdapter.ClassScheduleViewHolder> {
    private List<ClassScheduleModel> classes;
    private boolean isHomeScreen;
    private final OnClassScheduleActionListener listener;
    private View emptyView;

    public ClassScheduleAdapter(List<ClassScheduleModel> classes, boolean isHomeScreen, OnClassScheduleActionListener listener) {
        this.classes = classes != null ? classes : new ArrayList<>();
        this.isHomeScreen = isHomeScreen;
        this.listener = listener;
    }

    public interface OnClassScheduleActionListener {
        void onEditClassSchedule(ClassScheduleModel classes);
        void onDeleteClassSchedule(ClassScheduleModel classes);
    }

    @NonNull
    @Override
    public ClassScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class_schedule, parent, false);
        return new ClassScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassScheduleViewHolder holder, int position) {
        ClassScheduleModel Class = classes.get(position);
        holder.classScheduleName.setText(Class.getClassName());

        String details = "";
        if (!Class.getStartTime().isEmpty() && !Class.getEndTime().isEmpty()) {
            details = Class.getStartTime() + " ~ " + Class.getEndTime();
        } else if (!Class.getStartTime().isEmpty() && Class.getEndTime().isEmpty()) {
            details = Class.getStartTime() + " ~ ";
        } else if (Class.getStartTime().isEmpty() && !Class.getEndTime().isEmpty()) {
            details = "~ " + Class.getEndTime();
        }

        if (!Class.getClassPlace().isEmpty() && !details.isEmpty()) {
            details = Class.getClassPlace() + " | " + details;
        } else if (!Class.getClassPlace().isEmpty() && details.isEmpty()) {
            details = Class.getClassPlace();
        }

        if (!details.isEmpty()) {
            holder.classScheduleDetails.setText(details);
            holder.classScheduleDetails.setVisibility(View.VISIBLE);
        } else {
            holder.classScheduleDetails.setVisibility(View.GONE);
        }

        if (isHomeScreen) {
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
        } else {
            holder.editButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);
        }

        holder.editButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClassSchedule(Class);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClassSchedule(Class);
            }
        });
    }

    @Override
    public int getItemCount() {
        return classes.size();
    }

    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
        checkEmpty();
    }

    private void checkEmpty() {
        if (emptyView != null) {
            if (getItemCount() == 0) {
                emptyView.setVisibility(View.VISIBLE);
            } else {
                emptyView.setVisibility(View.GONE);
            }
        }
    }

    public void updateClassSchedules(List<ClassScheduleModel> newClasses) {
        this.classes = newClasses != null ? newClasses : new ArrayList<>();
        notifyDataSetChanged();
        checkEmpty();
    }

    public static class ClassScheduleViewHolder extends RecyclerView.ViewHolder {
        private final TextView classScheduleName, classScheduleDetails;
        private final ImageButton editButton, deleteButton;

        public ClassScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            classScheduleName = itemView.findViewById(R.id.class_schedule_name);
            classScheduleDetails = itemView.findViewById(R.id.class_schedule_details);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }

        public void bind(ClassScheduleModel classSchedule, OnClassScheduleActionListener listener) {
            classScheduleName.setText(classSchedule.getClassName());
            classScheduleDetails.setText(classSchedule.getClassPlace() + " | " + classSchedule.getStartTime() + " ~ " + classSchedule.getEndTime());
            editButton.setOnClickListener(v -> listener.onEditClassSchedule(classSchedule));
            deleteButton.setOnClickListener(v -> listener.onDeleteClassSchedule(classSchedule));
        }
    }
}