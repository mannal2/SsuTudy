package com.example.ssutudy;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CourseListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CourseListFragment extends Fragment {

    private static final String ARG_PARAM1 = "studentNum";
    private String studentNum;
    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter adapter;
    private RecyclerView recyclerView;
    private HashMap<String, String> courseList;
    private View previousView;
    private Animation slideIn;
    private Animation slideOut;

    public CourseListFragment() {}

    public static CourseListFragment newInstance(String studentNum) {
        CourseListFragment fragment = new CourseListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, studentNum);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            studentNum = getArguments().getString(ARG_PARAM1);
        }
        slideIn = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_top);
        slideOut = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out_top);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_course_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        courseList = new HashMap();
        previousView = null;

        Query query = db.collection("course")
                .whereEqualTo("studentNum", studentNum);

        //Log.d("html", "여기까지");

        FirestoreRecyclerOptions<Course> options = new FirestoreRecyclerOptions.Builder<Course>()
                .setQuery(query, Course.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Course, CourseHolder>(options) {
            @Override
            public void onBindViewHolder(@NonNull CourseHolder holder, int position, @NonNull Course model) {
                //Log.d("html", model.getCourseName());
                holder.bind(model);
                courseList.put(model.getCourseName(), model.getCourseNum());
            }

            @NonNull
            @Override
            public CourseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View contentView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.course_record_item, parent, false);

                contentView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(contentView.findViewById(R.id.buttonSet).getVisibility() == View.GONE){
                            contentView.findViewById(R.id.buttonSet).startAnimation(slideIn);
                            contentView.findViewById(R.id.buttonSet).setVisibility(View.VISIBLE);
                            if(previousView != null){
                                previousView.setVisibility(View.GONE);
                                previousView = contentView.findViewById(R.id.buttonSet);
                            }else previousView=contentView.findViewById(R.id.buttonSet);
                        }else {
                            contentView.findViewById(R.id.buttonSet).setVisibility(View.GONE);
                            previousView=null;
                        }
                    }
                });

                //버튼 클릭 이벤트
                Button rankBt = contentView.findViewById(R.id.to_rank);
                Button watchBt = contentView.findViewById(R.id.to_watch);
                TextView tv = contentView.findViewById(R.id.course_name);
                TextView tv2 = contentView.findViewById(R.id.course_number);

                rankBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String courseNum = tv2.getText().toString();
                        //Log.d("html", courseNum);
                        getParentFragmentManager().beginTransaction()
                                .add(R.id.fragment_container, CourseRankFragment.newInstance(tv.getText().toString(), courseList))
                                .commit();
                    }
                });

                watchBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       getParentFragmentManager().beginTransaction()
                               .add(R.id.fragment_container, CourseWatchFragment.newInstance(tv.getText().toString(), courseList, studentNum, false))
                               .commit();
                    }
                });


                return new CourseHolder(contentView);
            }
        };

        recyclerView = view.findViewById(R.id.course_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(adapter!=null) adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(adapter!=null) adapter.stopListening();
    }
}