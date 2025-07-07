package com.example.ssutudy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.A;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class RecordActivity extends AppCompatActivity {

    private String studentNum;
    private BottomNavigationView bottomNavigationView;
    private FirebaseFirestore db;
    private ArrayList<String> friendList;
    private SharedPreferences spf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        db = FirebaseFirestore.getInstance();
        friendList = new ArrayList<>();
        studentNum=getIntent().getStringExtra("studentNum");
        //studentNum = "20212985";
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.page_4);

        spf = getSharedPreferences("stopwatch", MODE_PRIVATE);
        boolean isStudying = spf.getBoolean("isStudying", false);

        if(isStudying){
            //원래 화면 복원
            String courseName= spf.getString("courseName", "0");
            HashMap<String, String> courseList = new HashMap<>();
            String jsonStr = spf.getString("jsonStr", (new JSONObject()).toString());
            try {
                JSONObject jo = new JSONObject(jsonStr);
                Iterator<String> keys = jo.keys();
                while(keys.hasNext()){
                    String key = keys.next();
                    String value = (String)jo.get(key);
                    courseList.put(key, value);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, CourseWatchFragment.newInstance(courseName, courseList, studentNum, true))
                    .commit();

        }else{
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, CourseListFragment.newInstance(studentNum))
                    .commit();
        }

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if(itemId == R.id.page_1){
                    Intent intent = new Intent(RecordActivity.this, HomeActivity.class);
                    intent.putExtra("studentNum", studentNum);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }
                if(itemId == R.id.page_2){
                    Intent intent = new Intent(RecordActivity.this, CalendarActivity.class);
                    intent.putExtra("studentNum", studentNum);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }
                if(itemId == R.id.page_3){
                    db.collection("friends").whereEqualTo("rootuser", studentNum).get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.isSuccessful()){
                                        for(QueryDocumentSnapshot document : task.getResult()){
                                            //Log.d("html", document.get("childuser").toString());
                                            friendList.add(document.get("childuser").toString());
                                        }
                                        Intent intent = new Intent(RecordActivity.this, FriendsActivity.class);
                                        intent.putExtra("studentNum", studentNum);
                                        intent.putExtra("friendList", friendList);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                        overridePendingTransition(0, 0);
                                    }
                                }
                            });
                    return true;
                }/*
                if(itemId == R.id.page_4){
                    Intent intent = new Intent(RecordActivity.this, RecordActivity.class);
                    intent.putExtra("studentNum", studentNum);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                }*/
                if(itemId == R.id.page_5){
                    Intent intent = new Intent(RecordActivity.this, SettingsActivity.class);
                    intent.putExtra("studentNum", studentNum);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            }
        });
    }
}