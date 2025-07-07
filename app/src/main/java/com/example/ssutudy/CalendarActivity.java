package com.example.ssutudy;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CalendarActivity extends AppCompatActivity {
    private MaterialCalendarView calendarView;
    private ScheduleAdapter scheduleAdapter;
    private TextView selectedDateTextView;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userID;
    private ArrayList<String> friendList;

    private Set<CalendarDay> selectedDates = new HashSet<>();
    private String selectedTime = "";
    private RecyclerView scheduleRecyclerView;
    private RecyclerView classScheduleRecyclerView;
    private ClassScheduleAdapter classScheduleAdapter;
    private RecyclerView categoryRecyclerView;
    private CategoryAdapter categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userID = getIntent().getStringExtra("studentNum");
        setContentView(R.layout.activity_calendar);

        calendarView = findViewById(R.id.calendar_view);
        selectedDateTextView = findViewById(R.id.selected_date);
        scheduleRecyclerView = findViewById(R.id.schedule_recycler_view);
        classScheduleRecyclerView = findViewById(R.id.class_schedule_recycler_view);
        categoryRecyclerView = findViewById(R.id.category_recycler_view);
        friendList = new ArrayList<>();

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        //sharedPreferences.edit().clear().apply();
        boolean uploadCompleted = sharedPreferences.getBoolean(KEY_UPLOAD_COMPLETED, false);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.page_2);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if(itemId == R.id.page_1){
                    Intent intent = new Intent(CalendarActivity.this, HomeActivity.class);
                    intent.putExtra("studentNum", userID);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }
                /*if(itemId == R.id.page_2){
                    Intent intent = new Intent(SettingsActivity.this, CalendarActivity.class);
                    intent.putExtra("studentNum", studentNum);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }*/
                if(itemId == R.id.page_3){
                    db.collection("friends").whereEqualTo("rootuser", userID).get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.isSuccessful()){
                                        for(QueryDocumentSnapshot document : task.getResult()){
                                            //Log.d("html", document.get("childuser").toString());
                                            friendList.add(document.get("childuser").toString());
                                        }
                                        Intent intent = new Intent(CalendarActivity.this, FriendsActivity.class);
                                        intent.putExtra("studentNum", userID);
                                        intent.putExtra("friendList", friendList);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                        overridePendingTransition(0, 0);
                                    }
                                }
                            });
                    return true;
                }
                if(itemId == R.id.page_4){
                    Intent intent = new Intent(CalendarActivity.this, RecordActivity.class);
                    intent.putExtra("studentNum", userID);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }

                if(itemId == R.id.page_5){
                    Intent intent = new Intent(CalendarActivity.this, SettingsActivity.class);
                    intent.putExtra("studentNum", userID);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            }
        });

//        //테스트용
//        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putBoolean(KEY_UPLOAD_COMPLETED, false); // 항상 false로 초기화
//        editor.apply();

        ImageButton addSchedule = findViewById(R.id.add_schedule);
        ImageButton addClassSchedule = findViewById(R.id.add_class_schedule);

        loadSchedules();
        loadClassSchedules();
        loadCategories();

        if (!uploadCompleted) {
            Log.d("CalendarActivity", "Uploading dialog will show.");
            showUploadDialog(sharedPreferences);
        } else {
            Log.d("CalendarActivity", "Upload already completed.");
        }

        scheduleRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        scheduleAdapter = new ScheduleAdapter(new ArrayList<>(), new ScheduleAdapter.OnScheduleActionListener() {
            @Override
            public void onEditSchedule(ScheduleModel schedule) {
                openEditScheduleDialog(schedule);
            }

            @Override
            public void onDeleteSchedule(ScheduleModel schedule) {
                openDeleteScheduleDialog(schedule);
            }
        }, true);

        scheduleRecyclerView.setAdapter(scheduleAdapter);

        addSchedule.setOnClickListener(view -> {
            openAddScheduleDialog();
        });

        classScheduleRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        classScheduleAdapter = new ClassScheduleAdapter(new ArrayList<>(), false, new ClassScheduleAdapter.OnClassScheduleActionListener() {
            @Override
            public void onEditClassSchedule(ClassScheduleModel classSchedule) { openEditClassScheduleDialog(classSchedule);}

            @Override
            public void onDeleteClassSchedule(ClassScheduleModel classSchedule) { openDeleteClassScheduleDialog(classSchedule);}
        });
        classScheduleRecyclerView.setAdapter(classScheduleAdapter);
        addClassSchedule.setOnClickListener(view -> {
            openAddClassScheduleDialog();
        });
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new CategoryAdapter(new ArrayList<>(), new CategoryAdapter.OnCategoryActionListener() {

            @Override
            public void onAddTodoDialog(String categoryId) {
                openAddTodoDialog(categoryId);
            }

            @Override
            public void onAddTodo(String categoryId, String todoName) {
                addTodoToCategory(categoryId, todoName);
            }

            @Override
            public void onTodoChecked(String categoryId, String todoId, boolean isChecked) {
                updateTodoCheckedStatus(categoryId, todoId, isChecked);
            }

            @Override
            public void onTodoEdit(String categoryId, String todoId) {
                openEditTodoDialog(categoryId, todoId);
            }

            @Override
            public void onTodoDelete(String categoryId, String todoId, String todoName) {
                openDeleteTodoDialog(categoryId, todoId, todoName);
            }
        });

        CalendarDay today = CalendarDay.today();

        calendarView.setDateSelected(today, true);
        updateSelectedDate(today);
        loadScheduleForDate();
        loadClassScheduleForDate();

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            selectedDates.clear();
            selectedDates.add(date);

            updateSelectedDate(date);

            loadScheduleForDate();
            loadClassScheduleForDate();
        });

        calendarView.setTitleFormatter(day -> day.getYear() + "년 " + day.getMonth() + "월");
        categoryRecyclerView.setAdapter(categoryAdapter);
    }
    private String formatCalendarDay(CalendarDay date) {
        return date.getYear() + "-" +
                String.format("%02d", date.getMonth()) + "-" + String.format("%02d", date.getDay());
    }

    private void updateSelectedDate(CalendarDay date) {
        String dateText = date.getYear() + "년 " + date.getMonth() + "월 " + date.getDay() + "일";
        selectedDateTextView.setText(dateText + " 일정");
    }

    private void openAddScheduleDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_schedule, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        EditText scheduleTitleInput = dialogView.findViewById(R.id.add_schedule_title);
        EditText schedulePlaceInput = dialogView.findViewById(R.id.add_schedule_place);
        Button timeButton = dialogView.findViewById(R.id.add_time_button);
        Button timeResetButton = dialogView.findViewById(R.id.add_time_reset_button);
        Button saveButton = dialogView.findViewById(R.id.save_button);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);

        timeButton.setOnClickListener(v -> {
            Calendar currentTime = Calendar.getInstance();
            int hour = currentTime.get(Calendar.HOUR_OF_DAY);
            int minute = currentTime.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, selectedMinute) -> {
                        selectedTime = String.format("%02d:%02d", hourOfDay, selectedMinute);
                        timeButton.setText(selectedTime);
                    },
                    hour,
                    minute,
                    true
            );
            timePickerDialog.show();
        });

        timeResetButton.setOnClickListener(v -> {
            selectedTime = "";
            timeButton.setText("시간 (선택)");
        });

        saveButton.setOnClickListener(v -> {
            String scheduleTitle = scheduleTitleInput.getText().toString();
            String schedulePlace = schedulePlaceInput.getText().toString();

            CalendarDay selectedDate = calendarView.getSelectedDate();
            if (selectedDate == null) {
                Log.e("CalendarActivity", "선택된 날짜가 없습니다.");
                return;
            }

            if (scheduleTitle.isEmpty()) {
                scheduleTitleInput.setError("일정명을 입력하세요.");
                return;
            }

            String dateKey = formatCalendarDay(selectedDate);
            addSchedule(dateKey, scheduleTitle, schedulePlace, selectedTime);

            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void loadSchedules() {
        db.collection("users")
                .document(userID)
                .collection("schedules")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    HashSet<CalendarDay> scheduleDates = new HashSet<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String date = document.getId();
                        try {
                            String[] parts = date.split("-");
                            int year = Integer.parseInt(parts[0]);
                            int month = Integer.parseInt(parts[1]) - 1;
                            int day = Integer.parseInt(parts[2]);

                            CalendarDay calendarDay = CalendarDay.from(year, month, day);
                            scheduleDates.add(calendarDay);
                        } catch (Exception e) {
                            Log.e("Firestore", "Invalid date format: " + date, e);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.w("Firestore", "사용자 일정 로드 실패", e));
    }

    private void loadScheduleForDate() {
        CalendarDay selectedDate = calendarView.getSelectedDate();
        if (selectedDate == null) {
            Log.e("CalendarActivity", "선택된 날짜가 없습니다.");
            return;
        }

        String dateKey = formatCalendarDay(selectedDate);

        db.collection("users")
                .document(userID)
                .collection("schedules")
                .document(dateKey)
                .collection("tasks")
                .orderBy("time")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<ScheduleModel> schedules = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        ScheduleModel schedule = document.toObject(ScheduleModel.class);
                        if (schedule != null) {
                            schedule.setId(document.getId());
                            schedules.add(schedule);
                        }
                    }
                    scheduleAdapter.updateSchedules(schedules);
                })
                .addOnFailureListener(e -> Log.w("Firestore", "일정 로드 실패", e));
    }

    private void addSchedule(String date, String title, String place, String time) {
        Map<String, Object> schedule = new HashMap<>();
        schedule.put("title", title);
        schedule.put("place", place);
        schedule.put("time", time);

        db.collection("users")
                .document(userID)
                .collection("schedules")
                .document(date)
                .collection("tasks")
                .add(schedule)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "일정 추가 성공: " + documentReference.getId());
                    CalendarDay selectedDate = calendarView.getSelectedDate();
                    if (selectedDate != null) {
                        loadScheduleForDate();
                    }
                })
                .addOnFailureListener(e -> Log.w("Firestore", "일정 추가 실패", e));
    }

    private void openEditScheduleDialog(ScheduleModel schedule) {
        if (schedule.getId() == null || schedule.getId().isEmpty()) {
            Log.e("EditTaskDialog", "Task ID 비었음");
            return;
        }

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_schedule, null);
        builder.setView(dialogView);

        EditText editTitle = dialogView.findViewById(R.id.edit_schedule_title);
        EditText editPlace = dialogView.findViewById(R.id.edit_schedule_place);
        Button timeButton = dialogView.findViewById(R.id.edit_time_button);
        Button timeResetButton = dialogView.findViewById(R.id.edit_time_reset_button);
        Button cancelButton = dialogView.findViewById(R.id.edit_cancel_button);
        Button updateButton = dialogView.findViewById(R.id.edit_update_button);

        editTitle.setText(schedule.getTitle());
        editPlace.setText(schedule.getPlace());
        final String[] selectedTime = {schedule.getTime()};

        if (schedule.getTime() != null && !schedule.getTime().isEmpty()) {
            timeButton.setText(schedule.getTime());
        } else {
            timeButton.setText("시간(선택)");
        }

        timeButton.setOnClickListener(v -> {
            Calendar currentTime = Calendar.getInstance();
            int hour = currentTime.get(Calendar.HOUR_OF_DAY);
            int minute = currentTime.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, selectedMinute) -> {
                        selectedTime[0] = String.format("%02d:%02d", hourOfDay, selectedMinute);
                        timeButton.setText(selectedTime[0]);
                    },
                    hour,
                    minute,
                    true
            );
            timePickerDialog.show();
        });

        timeResetButton.setOnClickListener(v -> {
            selectedTime[0] = "";
            timeButton.setText("시간(선택)");
        });

        android.app.AlertDialog dialog = builder.create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        updateButton.setOnClickListener(v -> {
            String newTitle = editTitle.getText().toString().trim();
            String newPlace = editPlace.getText().toString().trim();
            String newTime = selectedTime[0];

            if (newTitle.isEmpty()) {
                editTitle.setError("일정명을 입력하세요.");
                return;
            }

            updateScheduleInFirestore(schedule.getId(), newTitle, newPlace, newTime);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateScheduleInFirestore(String taskId, String title, String place, String time) {
        CalendarDay selectedDate = calendarView.getSelectedDate();
        if (selectedDate == null) {
            Log.e("CalendarActivity", "선택된 날짜가 없습니다.");
            return;
        }
        String dateKey = formatCalendarDay(selectedDate);

        if (taskId == null || taskId.isEmpty()) {
            Log.e("Firestore", "Task ID 비었음");
            return;
        }

        Map<String, Object> updatedSchedule = new HashMap<>();
        updatedSchedule.put("title", title);
        updatedSchedule.put("place", place);
        updatedSchedule.put("time", time);

        db.collection("users")
                .document(userID)
                .collection("schedules")
                .document(dateKey)
                .collection("tasks")
                .document(taskId)
                .update(updatedSchedule)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "일정 수정 완료!");
                    loadScheduleForDate();
                })
                .addOnFailureListener(e -> Log.w("Firestore", "일정 수정 오류", e));
    }

    private void openDeleteScheduleDialog(ScheduleModel schedule) {
        CalendarDay selectedDate = calendarView.getSelectedDate();
        if (selectedDate == null) {
            Log.e("CalendarActivity", "선택된 날짜가 없습니다.");
            return;
        }
        String dateKey = formatCalendarDay(selectedDate);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_schedule, null);
        builder.setView(dialogView);

        TextView scheduleTitle = dialogView.findViewById(R.id.delete_schedule_title);
        Button cancelButton = dialogView.findViewById(R.id.delete_cancel_button);
        Button deleteButton = dialogView.findViewById(R.id.delete_confirm_button);

        scheduleTitle.setText(schedule.getTitle());

        AlertDialog dialog = builder.create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        deleteButton.setOnClickListener(v -> {
            String taskId = schedule.getId();
            if (taskId == null || taskId.isEmpty()) {
                Log.e("DeleteSchedule", "Schedule ID is null or empty");
                dialog.dismiss();
                return;
            }

            db.collection("users")
                    .document(userID)
                    .collection("schedules")
                    .document(dateKey)
                    .collection("tasks")
                    .document(taskId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "Task deleted successfully");
                        loadScheduleForDate();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> Log.w("Firestore", "Error deleting task", e));
        });

        dialog.show();
    }

    private void parseAndUploadSchedule(String userID, String courseName, String courseTime) throws ParseException {
        if (courseTime == null || !courseTime.contains(" ") || !courseTime.contains("-")) {
            System.err.println("Invalid courseTime format: " + courseTime);
            return;
        }
        courseTime = courseTime.replace("\"", "").trim();

        String[] parts = courseTime.split(" ");
        if (parts.length < 2) {
            System.err.println("Invalid courseTime parts: " + Arrays.toString(parts));
            return;
        }

        String dayOfWeek = parts[0].trim();
        String[] timeParts = parts[1].trim().split("-");
        if (timeParts.length < 2) {
            System.err.println("Invalid time range in courseTime: " + courseTime);
            return;
        }

        String startTime = timeParts[0].trim();
        String endTime = timeParts[1].trim();

        String LectureRoom = parts[2].trim();
        String LectureRoomNum = parts[3].trim();
        String classPlace = LectureRoom + " " + LectureRoomNum;
        List<Date> dates = calculateDates(dayOfWeek, "2024-09-02", "2024-12-13");

        for (Date date : dates) {
            String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);

            Map<String, Object> scheduleData = new HashMap<>();
            scheduleData.put("className", courseName);
            scheduleData.put("startTime", startTime);
            scheduleData.put("endTime", endTime);
            scheduleData.put("classPlace", classPlace);

            db.collection("users")
                    .document(userID)
                    .collection("classSchedules")
                    .document(formattedDate)
                    .collection("tasks")
                    .add(scheduleData)
                    .addOnSuccessListener(documentReference -> {
                        System.out.println("Task added successfully with ID: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        System.err.println("Failed to add task for date: " + formattedDate);
                        e.printStackTrace();
                    });
        }
    }

    private List<Date> calculateDates(String day, String startDate, String endDate) {
        List<Date> dates = new ArrayList<>();
        Map<String, Integer> dayMap = new HashMap<>();
        dayMap.put("월", Calendar.MONDAY);
        dayMap.put("화", Calendar.TUESDAY);
        dayMap.put("수", Calendar.WEDNESDAY);
        dayMap.put("목", Calendar.THURSDAY);
        dayMap.put("금", Calendar.FRIDAY);

        int targetDay = dayMap.getOrDefault(day, -1);
        if (targetDay == -1) {
            Log.e("InvalidDay", "Invalid day provided: " + day);
            return dates;
        }

        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            startCal.setTime(sdf.parse(startDate));
            endCal.setTime(sdf.parse(endDate));
        } catch (Exception e) {
            Log.e("DateParsing", "날짜 파싱 실패", e);
            return dates;
        }

        while (!startCal.after(endCal)) {
            if (startCal.get(Calendar.DAY_OF_WEEK) == targetDay) {
                dates.add(startCal.getTime());
            }
            startCal.add(Calendar.DATE, 1);
        }

        return dates;
    }

    private static final String PREFS_NAME = "SsutudyPrefs";
    private static final String KEY_UPLOAD_COMPLETED = "UploadCompleted";

    private void showUploadDialog(SharedPreferences sharedPreferences) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("학사 일정 자동 업로드")
                .setMessage("학사 일정을 캘린더에 업로드하시겠습니까?")
                .setPositiveButton("업로드", (dialog, which) -> {
                    Log.d("CalendarActivity", "Upload initiated.");
                    db.collection("course")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (queryDocumentSnapshots.isEmpty()) {
                                    return;
                                }

                                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                    System.out.println("Document ID: " + document.getId());
                                    Map<String, Object> courseData = document.getData();
                                    if (courseData == null) {
                                        System.err.println("문서 내 강의 데이터 비었음 : " + document.getId());
                                        continue;
                                    }
                                    String firestoreStudentNum = String.valueOf(courseData.get("studentNum"));
                                    try {
                                        int firestoreNum = Integer.parseInt(firestoreStudentNum.trim());
                                        int userNum = Integer.parseInt(userID.trim());
                                        if (firestoreNum != userNum) {
                                            System.out.println("studentNum " + firestoreStudentNum + "과 userID 불일치 " + userID);
                                            continue;
                                        }
                                    } catch (NumberFormatException e) {
                                        System.err.println("적절하지 않은 학번 : " + firestoreStudentNum);
                                        continue;
                                    }
                                    String courseName = (String)courseData.get("courseName");
                                    if (courseName == null || courseName.isEmpty()) {
                                        System.err.println("courseName 존재하지 않음 : " + document.getId());
                                        continue;
                                    }

                                    System.out.println("Course Name: " + courseName);
                                    Object courseTimeObj = courseData.get("courseTime");
                                    if (!(courseTimeObj instanceof List)) {
                                        System.err.println("유효하지 않은 강의 시간 : " + courseName);
                                        continue;
                                    }
                                    List<String> courseTimes = (List<String>) courseTimeObj;
                                    for (String courseTime : courseTimes) {
                                        try {
                                            System.out.println("강의 시간 파싱 중  " + courseTime);
                                            parseAndUploadSchedule(userID, courseName, courseTime);
                                        } catch (Exception e) {
                                            System.err.println("강의 시간 파싱 에러 : " + courseTime);
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(KEY_UPLOAD_COMPLETED, true);
                    editor.apply();
                })
                .setNegativeButton("취소", (dialog, which) -> {
                    Log.d("CalendarActivity", "학사 일정 업로드 취소");
                    dialog.dismiss();
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void loadClassSchedules() {
        db.collection("users")
                .document(userID)
                .collection("classSchedules")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    HashSet<CalendarDay> classDates = new HashSet<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String date = document.getId();
                        try {
                            String[] parts = date.split("-");
                            int year = Integer.parseInt(parts[0]);
                            int month = Integer.parseInt(parts[1]) - 1;
                            int day = Integer.parseInt(parts[2]);

                            CalendarDay calendarDay = CalendarDay.from(year, month, day);
                            classDates.add(calendarDay);
                        } catch (Exception e) {
                            Log.e("Firestore", "유효하지 않은 날짜 형식 : " + date, e);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.w("Firestore", "학사 일정 로드 실패", e));
    }

    private void loadClassScheduleForDate() {
        CalendarDay selectedDate = calendarView.getSelectedDate();
        if (selectedDate == null) {
            Log.e("CalendarActivity", "선택된 날짜가 없습니다.");
            return;
        }

        String dateKey = formatCalendarDay(selectedDate);

        db.collection("users")
                .document(userID)
                .collection("classSchedules")
                .document(dateKey)
                .collection("tasks")
                .orderBy("startTime")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<ClassScheduleModel> classes = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        ClassScheduleModel classSchedule = document.toObject(ClassScheduleModel.class);
                        if (classSchedule != null) {
                            classSchedule.setId(document.getId());
                            classes.add(classSchedule);
                        }
                    }
                    classScheduleAdapter.updateClassSchedules(classes);
                })
                .addOnFailureListener(e -> Log.w("Firestore", "일정 로드 실패", e));
    }

    private void openAddClassScheduleDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_class_schedule, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        EditText classNameInput = dialogView.findViewById(R.id.add_class_name);
        EditText classPlaceInput = dialogView.findViewById(R.id.add_class_place);
        Button startTimeButton = dialogView.findViewById(R.id.add_class_start_time);
        Button endTimeButton = dialogView.findViewById(R.id.add_class_end_time);
        Button timeResetButton = dialogView.findViewById(R.id.add_time_reset_button);
        Button saveButton = dialogView.findViewById(R.id.save_button);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);

        final String[] startTime = {""};
        final String[] endTime = {""};
        startTimeButton.setOnClickListener(v -> {
            Calendar currentTime = Calendar.getInstance();
            int hour = currentTime.get(Calendar.HOUR_OF_DAY);
            int minute = currentTime.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, selectedMinute) -> {
                        String formattedTime = String.format("%02d:%02d", hourOfDay, selectedMinute);
                        startTimeButton.setText(formattedTime);
                        startTime[0] = formattedTime;
                    },
                    hour,
                    minute,
                    true
            );
            timePickerDialog.show();
        });

        endTimeButton.setOnClickListener(v -> {
            Calendar currentTime = Calendar.getInstance();
            int hour = currentTime.get(Calendar.HOUR_OF_DAY);
            int minute = currentTime.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, selectedMinute) -> {
                        String formattedTime = String.format("%02d:%02d", hourOfDay, selectedMinute);
                        endTimeButton.setText(formattedTime);
                        endTime[0] = formattedTime;
                    },
                    hour,
                    minute,
                    true
            );
            timePickerDialog.show();
        });

        timeResetButton.setOnClickListener(v -> {
            startTime[0] = "";
            endTime[0] = "";
            startTimeButton.setText("시작 시간");
            endTimeButton.setText("종료 시간");
        });

        saveButton.setOnClickListener(v -> {
            String className = classNameInput.getText().toString();
            String classPlace = classPlaceInput.getText().toString();

            CalendarDay selectedDate = calendarView.getSelectedDate();
            if (selectedDate == null) {
                Log.e("CalendarActivity", "선택된 날짜가 없습니다.");
                return;
            }

            if (className.isEmpty()) {
                classNameInput.setError("강의명을 입력하세요.");
                return;
            }

            if (!startTime[0].isEmpty() && !endTime[0].isEmpty()) {
                try {
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    Date start = timeFormat.parse(startTime[0]);
                    Date end = timeFormat.parse(endTime[0]);

                    if (start != null && end != null && end.before(start)) {
                        Log.e("CalendarActivity", "종료 시간이 시작 시간보다 이릅니다.");
                        Toast.makeText(this, "종료 시간이 시작 시간보다 이릅니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (ParseException e) {
                    Log.e("CalendarActivity", "시간 형식 파싱 오류", e);
                    Toast.makeText(this, "시간 형식에 오류가 있습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            String dateKey = formatCalendarDay(selectedDate);
            addClassSchedule(dateKey, className, classPlace, startTime[0], endTime[0]);

            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void addClassSchedule(String date, String className, String classPlace, String startTime, String endTime) {
        Map<String, Object> schedule = new HashMap<>();
        schedule.put("className", className);
        schedule.put("classPlace", classPlace);
        schedule.put("startTime", startTime);
        schedule.put("endTime", endTime);

        db.collection("users")
                .document(userID)
                .collection("classSchedules")
                .document(date)
                .collection("tasks")
                .add(schedule)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "일정 추가 성공: " + documentReference.getId());
                    CalendarDay selectedDate = calendarView.getSelectedDate();
                    if (selectedDate != null) {
                        loadClassScheduleForDate();
                    }
                })
                .addOnFailureListener(e -> Log.w("Firestore", "일정 추가 실패", e));
    }

    private void openEditClassScheduleDialog(ClassScheduleModel classSchedule) {
        if (classSchedule.getId() == null || classSchedule.getId().isEmpty()) {
            Log.e("EditClassScheduleDialog", "Class Schedule ID 비었음");
            return;
        }

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_class_schedule, null);
        builder.setView(dialogView);

        EditText editClassName = dialogView.findViewById(R.id.edit_class_name);
        EditText editPlace = dialogView.findViewById(R.id.edit_class_place);
        Button startTimeButton = dialogView.findViewById(R.id.edit_start_time_button);
        Button endTimeButton = dialogView.findViewById(R.id.edit_end_time_button);
        Button timeResetButton = dialogView.findViewById(R.id.edit_time_reset_button);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        Button updateButton = dialogView.findViewById(R.id.save_button);

        editClassName.setText(classSchedule.getClassName());
        editPlace.setText(classSchedule.getClassPlace());
        final String[] selectedStartTime = {classSchedule.getStartTime()};
        final String[] selectedEndTime = {classSchedule.getEndTime()};

        if (classSchedule.getStartTime() != null && !classSchedule.getStartTime().isEmpty()) {
            startTimeButton.setText(classSchedule.getStartTime());
        } else {
            startTimeButton.setText("시작 시간");
        }

        startTimeButton.setOnClickListener(v -> {
            Calendar currentTime = Calendar.getInstance();
            int hour = currentTime.get(Calendar.HOUR_OF_DAY);
            int minute = currentTime.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, selectedMinute) -> {
                        selectedStartTime[0] = String.format("%02d:%02d", hourOfDay, selectedMinute);
                        startTimeButton.setText(selectedStartTime[0]);
                    },
                    hour,
                    minute,
                    true
            );
            timePickerDialog.show();
        });

        if (classSchedule.getEndTime() != null && !classSchedule.getEndTime().isEmpty()) {
            endTimeButton.setText(classSchedule.getEndTime());
        } else {
            endTimeButton.setText("종료 시간");
        }

        endTimeButton.setOnClickListener(v -> {
            Calendar currentTime = Calendar.getInstance();
            int hour = currentTime.get(Calendar.HOUR_OF_DAY);
            int minute = currentTime.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, selectedMinute) -> {
                        selectedEndTime[0] = String.format("%02d:%02d", hourOfDay, selectedMinute);
                        endTimeButton.setText(selectedEndTime[0]);
                    },
                    hour,
                    minute,
                    true
            );
            timePickerDialog.show();
        });

        timeResetButton.setOnClickListener(v -> {
            selectedStartTime[0] = "";
            selectedEndTime[0] = "";
            startTimeButton.setText("시작 시간");
            endTimeButton.setText("종료 시간");
        });

        android.app.AlertDialog dialog = builder.create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        updateButton.setOnClickListener(v -> {
            String newClassName = editClassName.getText().toString().trim();
            String newClassPlace = editPlace.getText().toString().trim();
            String newStartTime = selectedStartTime[0];
            String newEndTime = selectedEndTime[0];

            if (newClassName.isEmpty()) {
                editClassName.setError("과목명을 입력하세요.");
                return;
            }

            if (!selectedStartTime[0].isEmpty() && !selectedEndTime[0].isEmpty()) {
                try {
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    Date start = timeFormat.parse(selectedStartTime[0]);
                    Date end = timeFormat.parse(selectedEndTime[0]);

                    if (start != null && end != null && end.before(start)) {
                        Log.e("CalendarActivity", "종료 시간이 시작 시간보다 이릅니다.");
                        Toast.makeText(this, "종료 시간이 시작 시간보다 이릅니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (ParseException e) {
                    Log.e("CalendarActivity", "시간 형식 파싱 오류", e);
                    Toast.makeText(this, "시간 형식에 오류가 있습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            updateClassScheduleInFirestore(classSchedule.getId(), newClassName, newClassPlace, newStartTime, newEndTime);
            dialog.dismiss();
        });

        dialog.show();
    }


    private void openDeleteClassScheduleDialog(ClassScheduleModel classSchedule) {
        CalendarDay selectedDate = calendarView.getSelectedDate();
        if (selectedDate == null) {
            Log.e("CalendarActivity", "선택된 날짜 없음");
            return;
        }
        String dateKey = formatCalendarDay(selectedDate);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_schedule, null);
        builder.setView(dialogView);

        TextView scheduleTitle = dialogView.findViewById(R.id.delete_schedule_title);
        Button cancelButton = dialogView.findViewById(R.id.delete_cancel_button);
        Button deleteButton = dialogView.findViewById(R.id.delete_confirm_button);

        scheduleTitle.setText(classSchedule.getClassName());

        AlertDialog dialog = builder.create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        deleteButton.setOnClickListener(v -> {
            String taskID = classSchedule.getId();
            if (taskID == null || taskID.isEmpty()) {
                Log.e("DeleteSchedule", "Class Schedule ID is null or empty");
                dialog.dismiss();
                return;
            }

            db.collection("users")
                    .document(userID)
                    .collection("classSchedules")
                    .document(dateKey)
                    .collection("tasks")
                    .document(taskID)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "Task deleted successfully");
                        loadClassScheduleForDate();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> Log.w("Firestore", "Error deleting task", e));
        });

        dialog.show();
    }

    private void updateClassScheduleInFirestore(String taskID, String className, String classPlace, String startTime, String endTime) {
        CalendarDay selectedDate = calendarView.getSelectedDate();
        if (selectedDate == null) {
            Log.e("CalendarActivity", "선택된 날짜가 없습니다.");
            return;
        }
        String dateKey = formatCalendarDay(selectedDate);

        if (taskID == null || taskID.isEmpty()) {
            Log.e("Firestore", "Task ID 비었음");
            return;
        }

        Map<String, Object> updatedClassSchedule = new HashMap<>();
        updatedClassSchedule.put("className", className);
        updatedClassSchedule.put("classPlace", classPlace);
        updatedClassSchedule.put("startTime", startTime);
        updatedClassSchedule.put("endTime", endTime);

        db.collection("users")
                .document(userID)
                .collection("classSchedules")
                .document(dateKey)
                .collection("tasks")
                .document(taskID)
                .update(updatedClassSchedule)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "학사 일정 수정 완료!");
                    loadClassScheduleForDate();
                })
                .addOnFailureListener(e -> Log.w("Firestore", "일정 수정 오류", e));
    }

    private void loadCategories() {
        List<CategoryModel> categories = new ArrayList<>();
        Log.d("LoadCategories", "카테고리 로딩 시작...");

        db.collection("category")
                .whereEqualTo("rootuser", userID)
                .get()
                .addOnSuccessListener(rootuserSnapshot -> {
                    for (DocumentSnapshot document : rootuserSnapshot.getDocuments()) {
                        processCategoryDocument(document, categories, () -> {
                            updateCategoriesUI(categories);
                        });
                    }

                    db.collection("category")
                            .whereArrayContains("member", userID)
                            .get()
                            .addOnSuccessListener(memberSnapshot -> {
                                for (DocumentSnapshot document : memberSnapshot.getDocuments()) {
                                    if (!categories.stream().anyMatch(cat -> cat.getId().equals(document.getId()))) {
                                        processCategoryDocument(document, categories, () -> {
                                            sortCategoriesAlphabetically(categories); // 가나다 순 정렬
                                            updateCategoriesUI(categories);
                                        });
                                    }
                                }
                            })
                            .addOnFailureListener(e -> Log.e("LoadCategories", "멤버로 카테고리 로드 실패", e));
                })
                .addOnFailureListener(e -> Log.e("LoadCategories", "rootuser로 카테고리 로드 실패", e));
    }
    private void sortCategoriesAlphabetically(List<CategoryModel> categories) {
        categories.sort((c1, c2) -> c1.getCategoryName().compareToIgnoreCase(c2.getCategoryName()));
    }

    private void processCategoryDocument(DocumentSnapshot document, List<CategoryModel> categories, Runnable onComplete) {
        String id = document.getId();
        String categoryName = document.getString("categoryName");
        List<TodoModel> todos = new ArrayList<>();
        String type = document.getString("type");

        db.collection("category").document(id).collection("todos")
                .orderBy("completed")
                .get()
                .addOnSuccessListener(todoSnapshot -> {
                    for (DocumentSnapshot todoDoc : todoSnapshot.getDocuments()) {
                        TodoModel todo = todoDoc.toObject(TodoModel.class);
                        if (todo != null) {
                            todo.setId(todoDoc.getId());
                            todos.add(todo);
                        }
                    }
                    categories.add(new CategoryModel(id, categoryName, todos, type));
                    Log.d("ProcessCategoryDocument", "Category loaded: " + categoryName + ", Todos: " + todos.size());
                    if (onComplete != null) {
                        onComplete.run();
                    }
                })
                .addOnFailureListener(e -> Log.e("ProcessCategoryDocument", "to do 로드 실패: " + id, e));
    }

    private void updateCategoriesUI(List<CategoryModel> categories) {
        runOnUiThread(() -> {
            if (categoryAdapter != null) {
                categoryAdapter.updateCategories(categories);
            } else {
                Log.e("UpdateCategoriesUI", "categoryAdapter is null");
            }
        });
    }

    private void openAddTodoDialog(String categoryId) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_todo, null);

        EditText todoNameInput = dialogView.findViewById(R.id.todo_name);
        Button saveButton = dialogView.findViewById(R.id.save_button);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        saveButton.setOnClickListener(v -> {
            String todoName = todoNameInput.getText().toString().trim();
            if (!todoName.isEmpty()) {
                addTodoToCategory(categoryId, todoName);
                dialog.dismiss();
            } else {
                todoNameInput.setError("할 일을 입력하세요.");
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void addTodoToCategory(String categoryId, String todoName) {
        TodoModel newTodo = new TodoModel(null, todoName, false);

        db.collection("category")
                .document(categoryId)
                .collection("todos")
                .add(newTodo)
                .addOnSuccessListener(documentReference -> {
                    String todoId = documentReference.getId();
                    documentReference.update("id", todoId)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("AddTodo", "Todo added successfully with ID: " + todoId);

                                for (CategoryModel category : categoryAdapter.getCategories()) {
                                    if (category.getId().equals(categoryId)) {
                                        category.getTodos().add(new TodoModel(todoId, todoName, false));
                                        break;
                                    }
                                }

                                categoryAdapter.notifyItemChanged(getCategoryIndexById(categoryId));
                            })
                            .addOnFailureListener(e -> Log.e("AddTodo", "Failed to update Todo ID", e));
                })
                .addOnFailureListener(e -> Log.e("AddTodo", "Failed to add Todo", e));
    }

    private int getCategoryIndexById(String categoryId) {
        for (int i = 0; i < categoryAdapter.getCategories().size(); i++) {
            if (categoryAdapter.getCategories().get(i).getId().equals(categoryId)) {
                return i;
            }
        }
        return -1;
    }

    private void updateTodoCheckedStatus(String categoryId, String todoId, boolean isChecked) {
        db.collection("category")
                .document(categoryId)
                .collection("todos")
                .document(todoId)
                .update("completed", isChecked)
                .addOnSuccessListener(aVoid -> Log.d("Todo", "Todo completion status updated successfully"))
                .addOnFailureListener(e -> Log.e("Todo", "Error updating todo completion status", e));
    }

    private void openEditTodoDialog(String categoryId, String todoId) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_todo, null);

        EditText todoTitleInput = dialogView.findViewById(R.id.edit_todo_title);
        Button updateButton = dialogView.findViewById(R.id.edit_update_button);
        Button cancelButton = dialogView.findViewById(R.id.edit_cancel_button);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true) // 다이얼로그 외부 클릭 시 닫히도록 설정
                .create();

        db.collection("category")
                .document(categoryId)
                .collection("todos")
                .document(todoId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    TodoModel todo = documentSnapshot.toObject(TodoModel.class);
                    if (todo != null) {
                        todoTitleInput.setText(todo.getTodoName());
                    }
                })
                .addOnFailureListener(e -> Log.e("EditTodo", "Failed to fetch todo details", e));

        updateButton.setOnClickListener(v -> {
            String updatedTitle = todoTitleInput.getText().toString().trim();
            if (!updatedTitle.isEmpty()) {
                db.collection("category")
                        .document(categoryId)
                        .collection("todos")
                        .document(todoId)
                        .update("todoName", updatedTitle)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("EditTodo", "Todo updated successfully");
                            dialog.dismiss();

                            for (CategoryModel category : categoryAdapter.getCategories()) {
                                if (category.getId().equals(categoryId)) {
                                    for (TodoModel todo : category.getTodos()) {
                                        if (todo.getId().equals(todoId)) {
                                            todo.setTodoName(updatedTitle);
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                            categoryAdapter.notifyDataSetChanged();
                        })
                        .addOnFailureListener(e -> Log.e("EditTodo", "Failed to update todo", e));
            } else {
                todoTitleInput.setError("일정을 입력하세요.");
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void openDeleteTodoDialog(String categoryId, String todoId, String todoName) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_todo, null);

        TextView todoNameView = dialogView.findViewById(R.id.todo_Name);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        Button deleteButton = dialogView.findViewById(R.id.save_button);

        todoNameView.setText(todoName);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        deleteButton.setOnClickListener(v -> {
            db.collection("category")
                    .document(categoryId)
                    .collection("todos")
                    .document(todoId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("DeleteTodo", "할 일 삭제 완료");
                        dialog.dismiss();

                        for (CategoryModel category : categoryAdapter.getCategories()) {
                            if (category.getId().equals(categoryId)) {
                                List<TodoModel> todos = category.getTodos();
                                for (int i = 0; i < todos.size(); i++) {
                                    if (todos.get(i).getId().equals(todoId)) {
                                        todos.remove(i);
                                        break;
                                    }
                                }
                                break;
                            }
                        }

                        categoryAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> Log.e("DeleteTodo", "Failed to delete todo", e));
        });

        dialog.show();
    }
}