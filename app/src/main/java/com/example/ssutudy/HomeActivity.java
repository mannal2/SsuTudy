package com.example.ssutudy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ssutudy.databinding.ActivityHomeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ClassScheduleAdapter classAdapter;
    private ScheduleAdapter scheduleAdapter;
    private TodoWithCategoryAdapter todoAdapter;
    private String userID;
    private ArrayList<String> friendList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userID = getIntent().getStringExtra("studentNum");

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        friendList = new ArrayList<>();

        setupClassRecyclerView();
        setupScheduleRecyclerView();

        setupTodoRecyclerView();
        loadClassSchedulesForToday();
        loadSchedulesForToday();
        loadIncompleteTodos();
        loadTotalStudyTime();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                /*if(itemId == R.id.page_1){
                    Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
                    intent.putExtra("studentNum", userID);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }*/
                if(itemId == R.id.page_2){
                    Intent intent = new Intent(HomeActivity.this, CalendarActivity.class);
                    intent.putExtra("studentNum", userID);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }
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
                                        Intent intent = new Intent(HomeActivity.this, FriendsActivity.class);
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
                    Intent intent = new Intent(HomeActivity.this, RecordActivity.class);
                    intent.putExtra("studentNum", userID);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }

                if(itemId == R.id.page_5){
                    Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                    intent.putExtra("studentNum", userID);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            }
        });
    }

    private void setupClassRecyclerView() {
        binding.classScheduleRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        classAdapter = new ClassScheduleAdapter(new ArrayList<>(), true, null);
        binding.classScheduleRecyclerView.setAdapter(classAdapter);
    }

    private void setupScheduleRecyclerView() {
        binding.homeScheduleRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        scheduleAdapter = new ScheduleAdapter(new ArrayList<>(), null, false);
        binding.homeScheduleRecyclerView.setAdapter(scheduleAdapter);
    }

    private void setupTodoRecyclerView() {
        binding.todoRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        todoAdapter = new TodoWithCategoryAdapter(new ArrayList<>()); // 제네릭 타입 명시
        binding.todoRecyclerView.setAdapter(todoAdapter);
    }

    private void loadClassSchedulesForToday() {
        CalendarDay today = CalendarDay.today();
        String dateKey = formatCalendarDay(today);

        db.collection("users")
                .document(userID)
                .collection("classSchedules")
                .document(dateKey)
                .collection("tasks")
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

                    classAdapter.updateClassSchedules(classes);

                    if (classes.isEmpty()) {
                        binding.classScheduleEmptyView.setVisibility(View.VISIBLE);
                        binding.classScheduleRecyclerView.setVisibility(View.GONE);
                    } else {
                        binding.classScheduleEmptyView.setVisibility(View.GONE);
                        binding.classScheduleRecyclerView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> Log.w("Firestore", "강의 일정 로드 실패", e));
    }

    private void loadSchedulesForToday() {
        CalendarDay today = CalendarDay.today();
        String dateKey = formatCalendarDay(today);

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

                    if (schedules.isEmpty()) {
                        binding.homeScheduleEmptyView.setVisibility(View.VISIBLE);
                        binding.homeScheduleRecyclerView.setVisibility(View.GONE);
                    } else {
                        binding.homeScheduleEmptyView.setVisibility(View.GONE);
                        binding.homeScheduleRecyclerView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> Log.w("Firestore", "일정 로드 실패", e));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null; // 메모리 누수 방지
    }

    private String formatCalendarDay(CalendarDay date) {
        return date.getYear() + "-" +
                String.format("%02d", date.getMonth()) + "-" + String.format("%02d", date.getDay());
    }

    private void loadIncompleteTodos() {
        List<TodoWithCategoryModel> incompleteTodos = new ArrayList<>();
        final int[] totalIncompleteCount = {0}; // 전체 미완료 할 일 개수 저장

        // rootuser 조건
        db.collection("category")
                .whereEqualTo("rootuser", userID)
                .get()
                .addOnSuccessListener(rootuserSnapshot -> {
                    for (DocumentSnapshot categoryDoc : rootuserSnapshot.getDocuments()) {
                        String categoryId = categoryDoc.getId();
                        String categoryName = categoryDoc.getString("categoryName");

                        db.collection("category")
                                .document(categoryId)
                                .collection("todos")
                                .whereEqualTo("completed", false)
                                .get()
                                .addOnSuccessListener(todoSnapshot -> {
                                    int categoryIncompleteCount = 0;

                                    for (DocumentSnapshot todoDoc : todoSnapshot.getDocuments()) {
                                        TodoWithCategoryModel todo = new TodoWithCategoryModel(
                                                categoryName,
                                                todoDoc.getString("todoName")
                                        );
                                        incompleteTodos.add(todo);
                                        categoryIncompleteCount++;
                                    }

                                    // 전체 미완료 할 일 개수 업데이트
                                    totalIncompleteCount[0] += categoryIncompleteCount;

                                    // UI 업데이트
                                    todoAdapter.updateTodos(incompleteTodos);
                                    updateRemainingTasks(totalIncompleteCount[0]);

                                    if (incompleteTodos.isEmpty()) {
                                        binding.emptyTodoView.setVisibility(View.VISIBLE);
                                        binding.todoRecyclerView.setVisibility(View.GONE);
                                    } else {
                                        binding.emptyTodoView.setVisibility(View.GONE);
                                        binding.todoRecyclerView.setVisibility(View.VISIBLE);
                                    }
                                })
                                .addOnFailureListener(e -> Log.e("Firestore", "할 일 로드 실패: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Failed to load categories by rootuser", e));

        // member 조건
        db.collection("category")
                .whereArrayContains("member", userID)
                .get()
                .addOnSuccessListener(memberSnapshot -> {
                    for (DocumentSnapshot categoryDoc : memberSnapshot.getDocuments()) {
                        String categoryId = categoryDoc.getId();
                        String categoryName = categoryDoc.getString("categoryName");

                        db.collection("category")
                                .document(categoryId)
                                .collection("todos")
                                .whereEqualTo("completed", false)
                                .get()
                                .addOnSuccessListener(todoSnapshot -> {
                                    int categoryIncompleteCount = 0;

                                    for (DocumentSnapshot todoDoc : todoSnapshot.getDocuments()) {
                                        TodoWithCategoryModel todo = new TodoWithCategoryModel(
                                                categoryName,
                                                todoDoc.getString("todoName")
                                        );
                                        incompleteTodos.add(todo);
                                        categoryIncompleteCount++;
                                    }

                                    // 전체 미완료 할 일 개수 업데이트
                                    totalIncompleteCount[0] += categoryIncompleteCount;

                                    // UI 업데이트
                                    todoAdapter.updateTodos(incompleteTodos);
                                    updateRemainingTasks(totalIncompleteCount[0]);

                                    if (incompleteTodos.isEmpty()) {
                                        binding.emptyTodoView.setVisibility(View.VISIBLE);
                                        binding.todoRecyclerView.setVisibility(View.GONE);
                                    } else {
                                        binding.emptyTodoView.setVisibility(View.GONE);
                                        binding.todoRecyclerView.setVisibility(View.VISIBLE);
                                    }
                                })
                                .addOnFailureListener(e -> Log.e("Firestore", "할 일 로드 실패: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Failed to load categories by member", e));
    }

    private void updateRemainingTasks(int incompleteCount) {
        String remainingText = "남은 할 일 " + incompleteCount + "개 !";
        binding.progressText.setText(remainingText);
    }

    private void loadTotalStudyTime() {
        db.collection("users")
                .document(userID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long totalStudyTime = documentSnapshot.getLong("totalStudyTime");
                        if (totalStudyTime != null) {
                            updateStudyTimeUI(totalStudyTime);
                        } else {
                            updateStudyTimeUI(0L);
                        }
                    } else {
                        Log.e("StudyTime", "사용자 문서를 찾을 수 없음");
                        updateStudyTimeUI(0L);
                    }
                })
                .addOnFailureListener(e -> Log.e("StudyTime", "Firestore 데이터 로드 실패: " + e.getMessage()));
    }

    private void updateStudyTimeUI(Long totalMinutes) {
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        String formattedTime = String.format("%d시간 %d분", hours, minutes);
        binding.totalStudyTime.setText(formattedTime);
    }
}