package com.example.ssutudy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONObject;
import org.w3c.dom.Document;

import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CourseWatchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CourseWatchFragment extends Fragment {

    private static final String ARG_PARAM1 = "courseName";
    private static final String ARG_PARAM2 = "courseList";
    private static final String ARG_PARAM3 = "studentNum";
    private static final String ARG_PARAM4 = "isStudying";

    // TODO: Rename and change types of parameters
    private String courseName;
    private String courseNum;
    private String studentNum;
    private HashMap courseList;
    private FirebaseFirestore db;
    private Spinner spinner;
    private Button writeBtn, startBtn;
    private boolean isRunning = false;
    private boolean isStudying;
    private TextView timetext;
    private TextView placetext;
    private boolean first;
    private SharedPreferences spf;

    public CourseWatchFragment() {
        // Required empty public constructor
    }

    private final BroadcastReceiver stopwatchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long elapsedTime = intent.getLongExtra("elapsedTime", 0L);
            updateStopwatchUI(elapsedTime);
        }
    };

    public static CourseWatchFragment newInstance(String param1, HashMap<String, String> param2, String param3, boolean param4) {
        CourseWatchFragment fragment = new CourseWatchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putSerializable(ARG_PARAM2, param2);
        args.putString(ARG_PARAM3, param3);
        args.putBoolean(ARG_PARAM4, param4);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            courseName = getArguments().getString(ARG_PARAM1);
            courseList = (HashMap) getArguments().getSerializable(ARG_PARAM2);
            courseNum = courseList.get(courseName).toString();
            studentNum = getArguments().getString(ARG_PARAM3);
            isStudying = getArguments().getBoolean(ARG_PARAM4);
        }
        spf = requireContext().getSharedPreferences("stopwatch", Context.MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_course_watch, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        spinner = (Spinner) view.findViewById(R.id.course_watch_spinner);
        writeBtn = view.findViewById(R.id.write);
        startBtn = view.findViewById(R.id.start_stop_continue);
        timetext = view.findViewById(R.id.circle_time_text);
        placetext = view.findViewById(R.id.course_watch_editText);
        Object[] array = courseList.keySet().toArray();
        String[] stringArray = new String[array.length];

        first = true;
        if(isStudying){
            isRunning = true;
            first=false;
            startBtn.setText("중지");
            placetext.setEnabled(false);
            spinner.setEnabled(false);
        }

        for (int i = 0; i < array.length; i++) {
            stringArray[i] = array[i].toString();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, stringArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        int position = adapter.getPosition(courseName);
        spinner.setSelection(position);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                courseName = stringArray[position];
                courseNum = courseList.get(courseName).toString();
                //Log.d("html", courseName + "---" + courseNum);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        writeBtn.setOnClickListener(v -> writeStopwatch());
        startBtn.setOnClickListener(v -> toggleStopwatch());
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(stopwatchReceiver, new IntentFilter("STOPWATCH_UPDATE"));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(requireContext())
                .unregisterReceiver(stopwatchReceiver);
    }

    private void toggleStopwatch(){
        Intent intent = new Intent(requireContext(), StopwatchService.class);
        if(first){
            first=false;
            //원복하기 위한 데이터 저장
            SharedPreferences.Editor editor = spf.edit();
            editor.putBoolean("isStudying", true);
            editor.putString("courseName", courseName);
            JSONObject jo = new JSONObject(courseList);
            String jsonStr = jo.toString();
            editor.putString("jsonStr", jsonStr);
            editor.apply();

            //공부상태 업데이트
            String place = placetext.getText().toString();
            placetext.setEnabled(false);
            spinner.setEnabled(false);
            if(place.isEmpty()) place="공부중";
            else place = place+"에서 공부중";
            db.collection("users").document(studentNum).update(
                    "state",place
            );

        }
        if(isRunning){
            writeBtn.setEnabled(true);
            intent.setAction("PAUSE");
            startBtn.setText("시작");
            isRunning=false;
        }else{
            writeBtn.setEnabled(false);
            intent.setAction("START");
            startBtn.setText("중지");
            isRunning=true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(intent);
        }else{
            requireContext().startService(intent);
        }
    }

    private void writeStopwatch(){
        Intent intent = new Intent(requireContext(), StopwatchService.class);
        intent.setAction("RESET");
        first=true;
        spf.edit().clear().apply();
        writeBtn.setEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(intent);
        }else{
            requireContext().startService(intent);
        }
        //DB로 전송
        int min;
        String text = timetext.getText().toString();
        int colonIndex = text.indexOf(':');
        if (colonIndex != -1) {
            String numberPart = text.substring(0, colonIndex);
            min = Integer.parseInt(numberPart);
        } else {
            min=0;
        }

        db.collection("course").whereEqualTo("courseNum", courseNum)
                .whereEqualTo("studentNum", studentNum).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()){
                                    for(QueryDocumentSnapshot document:task.getResult()){
                                        int t = Integer.parseInt(document.getData().get("courseStudyTime").toString());
                                        t += min;
                                        db.collection("course").document(document.getId()).update(
                                                "courseStudyTime", t
                                        );
                                    }
                                }else{
                                    Log.d("html", "Error getting documents: ", task.getException());
                                }
                            }
                        });

        db.collection("users").document(studentNum).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()){
                                int t = Integer.parseInt(document.getData().get("totalStudyTime").toString());
                                t += min;

                                db.collection("users").document(studentNum).update(
                                        "totalStudyTime", t,
                                        "state" ,"쉬는중"
                                );
                            }
                        }
                    }
                });

        //공부상태 업데이트
        placetext.setText("");
        placetext.setEnabled(true);
        spinner.setEnabled(true);
        //Log.d("html", "공부시간:"+studyTime);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, CourseListFragment.newInstance(studentNum))
                .commit();

    }

    @Override
    public void onStop() {
        super.onStop();
        if(!isRunning) {
            Intent intent = new Intent(requireContext(), StopwatchService.class);
            intent.setAction("RESET");
            first = true;
            spf.edit().clear().apply();
            writeBtn.setEnabled(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireContext().startForegroundService(intent);
            } else {
                requireContext().startService(intent);
            }
            db.collection("users").document(studentNum).update(
                    "state" ,"쉬는중"
            );
        }
    }

    private void updateStopwatchUI(long elapsedTime){
        int seconds = (int) (elapsedTime / 1000) % 60;
        int minutes = (int) (elapsedTime / (1000 * 60));
        String time = String.format("%02d:%02d", minutes, seconds);
        //Log.d("html", elapsedTime+"");
        timetext.setText(time);
    }

}