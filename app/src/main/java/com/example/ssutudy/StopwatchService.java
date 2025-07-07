package com.example.ssutudy;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class StopwatchService extends Service {
    private Handler handler = new Handler();
    private long startTime = 0L;
    private long elapsedTime = 0L; // 일시 정지된 시간 포함
    private boolean isRunning = false;

    private NotificationManager notificationManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            //setForegroundServiceType();  // 예시로 데이터 동기화 사용
        }
    }

    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "Stopwatch Channel";
            String description = "Channel for Stopwatch notifications";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel("STOPWATCH_CHANNEL", name, importance);
            channel.setDescription(description);

            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "STOPWATCH_CHANNEL")
                .setContentTitle("공부시간 측정중")
                .setContentText("스탑워치가 작동중입니다.")
                .setSmallIcon(R.drawable.ic_stopwatch)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true);

        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        return builder.build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if ("START".equals(action)) {
            //Log.d("html", "서비스의 스타트");
            startStopwatch();
        } else if ("PAUSE".equals(action)) {
            //Log.d("html", "서비스의 퍼즈");
            pauseStopwatch();
        } else if ("RESET".equals(action)) {
            //Log.d("html", "서비스의 리셋");
            resetStopwatch();
        }
        return START_STICKY;
    }


    private void startStopwatch() {
        if (!isRunning) {
            startTime = SystemClock.elapsedRealtime() - elapsedTime;
            isRunning = true;

            Notification notification = createNotification();
            startForeground(1, notification);
            handler.post(updateTimeRunnable);
        }
    }

    private void pauseStopwatch() {
        if (isRunning) {
            elapsedTime = SystemClock.elapsedRealtime() - startTime;
            isRunning = false;
            handler.removeCallbacks(updateTimeRunnable);
        }
    }

    private void resetStopwatch() {
        isRunning = false;
        handler.removeCallbacks(updateTimeRunnable);
        elapsedTime = 0L;
        sendElapsedTime(0L);

        stopForeground(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        handler.removeCallbacks(updateTimeRunnable);
        stopForeground(true);
    }

    private final Runnable updateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                long currentElapsedTime = SystemClock.elapsedRealtime() - startTime;
                sendElapsedTime(currentElapsedTime);
                handler.postDelayed(this, 1000);
            }
        }
    };

    private void sendElapsedTime(long time) {
        Intent intent = new Intent("STOPWATCH_UPDATE");
        intent.putExtra("elapsedTime", time);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        if(notificationManager != null){
            Notification notification = createNotification();
            notificationManager.notify(1, notification);
        }
    }
}
