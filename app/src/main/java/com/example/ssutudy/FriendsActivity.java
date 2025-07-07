package com.example.ssutudy;

import static com.google.android.gms.common.internal.safeparcel.SafeParcelable.NULL;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.example.ssutudy.databinding.ActivityFriendsBinding;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter adapter;
    private ArrayList<String> friendList;
    private String studentNum;
    private ActivityFriendsBinding binding;
    private String myname;
    private Dialog dialog;
    private RecyclerView recyclerView;
    private BottomNavigationView bottomNavigationView;

    private int count;
    private int rank;
    private int preMin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFriendsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_friend);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.page_3);

        friendList = getIntent().getStringArrayListExtra("friendList");
        studentNum = getIntent().getStringExtra("studentNum");
        db = FirebaseFirestore.getInstance();

        count=1;
        rank=1;

        db.collection("users").document(studentNum).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()){
                                binding.myAlias.setText(document.get("alias").toString());
                                binding.myState.setText(document.get("state").toString());
                                myname = document.get("name").toString();
                                String tt = document.get("totalStudyTime")+" Min";
                                binding.myStudytime.setText(tt);
                                String url = document.get("image_url").toString();
                                if(!url.equals("blank")){
                                    Glide.with(FriendsActivity.this).load(url).into(binding.myImage);
                                }
                            }else{
                                Log.d("html", "오류");
                            }
                        }
                    }
                });
        if(friendList.size() != 0) {
            Query query = db.collection("users")
                    .whereIn("studentNum", friendList)
                    .orderBy("totalStudyTime", Query.Direction.DESCENDING);
/*
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            //Log.d("html", document.getData().get("name").toString());
                        }
                    }
                }
            });*/

            FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                    .setQuery(query, User.class)
                    .build();


            adapter = new FirestoreRecyclerAdapter<User, UserHolder>(options) {

                @Override
                public void onDataChanged() {
                    super.onDataChanged();
                    count=rank=1;
                    notifyDataSetChanged();
                }

                @Override
                protected void onBindViewHolder(@NonNull UserHolder holder, int position, @NonNull User model) {
                    if(count==1) preMin = model.getTotalStudyTime();
                    else {
                        if(preMin != model.getTotalStudyTime()) rank=count;
                        preMin = model.getTotalStudyTime();
                        //Log.d("html", rank+"--"+count);
                    }
                    count++;
                    holder.setRankingField(rank);
                    holder.bind(model);
                    //Glide.with(InitActivity.this).load(uri2).into(binding.imageView);

                    if (model.getImage_url() == null || model.getImage_url().equals("blank"))
                        holder.imageField.setImageResource(R.drawable.circle);
                    else
                        Glide.with(holder.itemView.getContext()).load(model.getImage_url()).into(holder.imageField);
                }

                @NonNull
                @Override
                public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.friend_rank_item, parent, false);
                    return new UserHolder(view);
                }
            };

            recyclerView = findViewById(R.id.friend_rank_recycler);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
        }

        binding.plusFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if(itemId == R.id.page_1){
                    Intent intent = new Intent(FriendsActivity.this, HomeActivity.class);
                    intent.putExtra("studentNum", studentNum);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }
                if(itemId == R.id.page_2){
                    Intent intent = new Intent(FriendsActivity.this, CalendarActivity.class);
                    intent.putExtra("studentNum", studentNum);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }
                /*
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
                                        Intent intent = new Intent(FriendsActivity.this, FriendsActivity.class);
                                        intent.putExtra("studentNum", studentNum);
                                        intent.putExtra("friendList", friendList);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }
                                }
                            });
                    return true;
                }*/
                if(itemId == R.id.page_4){
                    Intent intent = new Intent(FriendsActivity.this, RecordActivity.class);
                    intent.putExtra("studentNum", studentNum);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }
                if(itemId == R.id.page_5){
                    Intent intent = new Intent(FriendsActivity.this, SettingsActivity.class);
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

    @Override
    protected void onStart() {
        super.onStart();
        if(adapter!=null) adapter.startListening();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(adapter!=null) adapter.stopListening();
    }

    public void showDialog(){
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.show();

        Button cancleBT = dialog.findViewById(R.id.add_friend_cancel_button);
        Button okBT = dialog.findViewById(R.id.add_friend_ok_button);

        cancleBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        okBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et = dialog.findViewById(R.id.friend_stNum);
                String friend_stNum = et.getText().toString();
                if(friend_stNum.isEmpty()){
                    Toast.makeText(FriendsActivity.this, "학번을 입력해주세요.",Toast.LENGTH_SHORT).show();
                    return;
                }
                Map<String, Object> data = new HashMap<>();
                data.put("receiverStudentNum", friend_stNum);
                data.put("senderStudentNum", studentNum);
                data.put("senderName", myname);
                db.collection("request_friend").add(data)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                et.setText("");
                                Toast.makeText(FriendsActivity.this, "친구신청 완료",Toast.LENGTH_SHORT).show();
                                dialog.dismiss();

                            }
                        });

            }
        });
    }
}