package com.example.ssutudy;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    SharedPreferences spf;
    String studentNum;
    String limitDate;
    boolean goHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        goHome=false;
        SharedPreferences stopwatch = getSharedPreferences("stopwatch", MODE_PRIVATE);
        stopwatch.edit().clear().apply();
        spf = getSharedPreferences("auto_login", Context.MODE_PRIVATE);
        //spf.edit().clear().apply();
        studentNum = spf.getString("studentNum", "none");
        limitDate = spf.getString("limitDate", "none");
        //limitDate="2024-12-06";
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        if(limitDate.equals("none")||studentNum.equals("none")){
            Log.d("html", "정보업슴");
            goLoginActivity();
        }else{
            try {
                Date old = formatter.parse(limitDate);
                Log.d("html", limitDate);
                if(old.after(now)) {
                    goHomeActivity();
                }else{
                    goLoginActivity();
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }
    void goHomeActivity(){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                    startActivity(new Intent(MainActivity.this, HomeActivity.class)
                            .putExtra("studentNum", studentNum)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        t.start();
    }

    void goLoginActivity(){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                    startActivity(new Intent(MainActivity.this, LoginActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        t.start();
    }
}