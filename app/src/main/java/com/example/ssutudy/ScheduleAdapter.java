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

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {
    private List<ScheduleModel> schedules;
    private final OnScheduleActionListener listener;
    private final boolean showActionButtons; // 버튼 표시 여부 플래그
    private View emptyView;

    public ScheduleAdapter(List<ScheduleModel> schedules, OnScheduleActionListener listener, boolean showActionButtons) {
        this.schedules = schedules != null ? schedules : new ArrayList<>();
        this.listener = listener;
        this.showActionButtons = showActionButtons; // 버튼 표시 여부 저장
    }

    public interface OnScheduleActionListener {
        void onEditSchedule(ScheduleModel schedule);
        void onDeleteSchedule(ScheduleModel schedule);
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        ScheduleModel schedule = schedules.get(position);
        holder.scheduleTitle.setText(schedule.getTitle());

        String details = "";
        if (!schedule.getPlace().isEmpty() && !schedule.getTime().isEmpty()) {
            details = schedule.getPlace() + " | " + schedule.getTime();
        } else if (!schedule.getPlace().isEmpty()) {
            details = schedule.getPlace();
        } else if (!schedule.getTime().isEmpty()) {
            details = schedule.getTime();
        }

        if (!details.isEmpty()) {
            holder.scheduleDetails.setText(details);
            holder.scheduleDetails.setVisibility(View.VISIBLE);
        } else {
            holder.scheduleDetails.setVisibility(View.GONE);
        }

        // 버튼의 가시성 설정
        if (showActionButtons) {
            holder.editButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);
        } else {
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
        }

        holder.editButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditSchedule(schedule);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteSchedule(schedule);
            }
        });
    }

    @Override
    public int getItemCount() {
        return schedules.size();
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

    public void updateSchedules(List<ScheduleModel> newSchedules) {
        this.schedules = newSchedules != null ? newSchedules : new ArrayList<>();
        notifyDataSetChanged();
        checkEmpty();
    }

    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        private final TextView scheduleTitle, scheduleDetails;
        private final ImageButton editButton, deleteButton;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            scheduleTitle = itemView.findViewById(R.id.task_title);
            scheduleDetails = itemView.findViewById(R.id.task_details);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}