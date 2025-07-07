package com.example.ssutudy;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
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

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CourseRankFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CourseRankFragment extends Fragment {


    private static final String ARG_PARAM1 = "courseName";
    private static final String ARG_PARAM2 = "courseList";

    // TODO: Rename and change types of parameters
    private String courseName;
    private String courseNum;
    private HashMap courseList;
    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter recyclerAdapter;
    private RecyclerView recyclerView;
    private Spinner spinner;
    Button btn;
    private int count;
    private int rank;
    private int preMin;

    public CourseRankFragment() {}

    public static CourseRankFragment newInstance(String param1, HashMap<String, String> param2) {
        CourseRankFragment fragment = new CourseRankFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putSerializable(ARG_PARAM2, param2);
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
        }
        count=1;
        rank=1;
        //Log.d("html", courseNum+"");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_course_rank, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        spinner = (Spinner) view.findViewById(R.id.course_rank_spinner);
        btn = view.findViewById(R.id.back);
        Object[] array = courseList.keySet().toArray();
        String[] stringArray = new String[array.length];

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction()
                        .remove(CourseRankFragment.this)
                        .commit();
            }
        });

        for (int i = 0; i < array.length; i++) {
            stringArray[i] = array[i].toString();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, stringArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        int position = adapter.getPosition(courseName);
        spinner.setSelection(position);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                courseName = stringArray[position];
                courseNum = courseList.get(courseName).toString();
                Query updatedQuery = db.collection("course")
                        .whereEqualTo("courseNum", courseNum)
                        .orderBy("courseStudyTime", Query.Direction.DESCENDING);


                FirestoreRecyclerOptions<Course> newOptions = new FirestoreRecyclerOptions.Builder<Course>()
                        .setQuery(updatedQuery, Course.class)
                        .build();
                recyclerAdapter.stopListening();
                recyclerView.setAdapter(null);
                recyclerAdapter.updateOptions(newOptions);
                recyclerView.setAdapter(recyclerAdapter);
                recyclerAdapter.startListening();
                //Log.d("html", "courseName:"+courseName+"---"+courseNum);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        db = FirebaseFirestore.getInstance();
        Query query = db.collection("course")
                .whereEqualTo("courseNum", courseNum)
                .orderBy("courseStudyTime", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Course> options = new FirestoreRecyclerOptions.Builder<Course>()
                .setQuery(query, Course.class)
                .build();

        recyclerAdapter = new FirestoreRecyclerAdapter<Course, CourseHolder2>(options) {

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                count=rank=1;
                notifyDataSetChanged();
            }

            @Override
            protected void onBindViewHolder(@NonNull CourseHolder2 holder, int position, @NonNull Course model) {
                holder.bind(model);
                if(count==1) preMin = model.getCourseStudyTime();
                else {
                    if(preMin != model.getCourseStudyTime()) rank=count;
                    preMin = model.getCourseStudyTime();
                    Log.d("html", rank+"--"+count);
                }
                count++;
                holder.setRanking(rank);

                String studentNum = model.getStudentNum();
                Log.d("html", studentNum);
                db.collection("users").document(studentNum).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    DocumentSnapshot document = task.getResult();
                                    if(document.exists()){
                                        holder.setAlias(document.get("alias").toString());
                                        String url = document.get("image_url").toString();
                                        if(!url.equals("blank")){
                                            Glide.with(requireActivity()).load(url).into(holder.courseRankImageField);
                                        }
                                    }
                                }
                            }
                        });
            }

            @NonNull
            @Override
            public CourseHolder2 onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View contentView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.course_rank_item, parent, false);
                return new CourseHolder2(contentView);
            }
        };
        recyclerView = view.findViewById(R.id.course_rank_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(recyclerAdapter);



    }

    @Override
    public void onStart() {
        super.onStart();
        if(recyclerAdapter!=null) recyclerAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(recyclerAdapter!=null) recyclerAdapter.stopListening();
    }
}