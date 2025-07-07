package com.example.ssutudy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ssutudy.databinding.ActivityFriendRequestDialogBinding;
import com.example.ssutudy.databinding.ItemFriendRequestBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.collections.ArrayDeque;

public class FriendRequestDialogActivity extends AppCompatActivity {

    private String studentNum;
    private FirebaseFirestore db;
    private FriendsAdapter adapter;
    private List<String> friendRequests = new ArrayList<>();
    private ActivityFriendRequestDialogBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       binding = ActivityFriendRequestDialogBinding.inflate(getLayoutInflater());
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(binding.getRoot());

        ItemFriendRequestBinding itemBinding = ItemFriendRequestBinding.inflate(getLayoutInflater());

        studentNum = getIntent().getStringExtra("studentNum");
        db = FirebaseFirestore.getInstance();

        adapter = new FriendsAdapter(friendRequests);

        binding.friendRequestRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.friendRequestRecyclerView.setAdapter(adapter);

        loadFriendRequests(friendRequests);


        binding.dialogCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void loadFriendRequests(List<String> friendRequests){
        ActivityFriendRequestDialogBinding binding = ActivityFriendRequestDialogBinding.inflate(getLayoutInflater());
        db.collection("request_friend")
                .whereEqualTo("receiverStudentNum", studentNum)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        friendRequests.clear();
                        for (DocumentSnapshot document : task.getResult()){
                            String senderName = document.getString("senderName");
                            String senderStudentNum = document.getString("senderStudentNum");
                            if (senderName != null && senderStudentNum != null){
                                friendRequests.add(senderName+" ("+senderStudentNum+")");
                            }
                        }
                        adapter.notifyDataSetChanged();
                        checkEmptyView(binding, friendRequests);
                    }
                })
                .addOnFailureListener(e ->{
                    e.printStackTrace();
                });
    }

    private static class FriendsViewHolder extends RecyclerView.ViewHolder{
        private final ItemFriendRequestBinding binding;
        private FriendsViewHolder(ItemFriendRequestBinding binding){
            super(binding.getRoot());
            this.binding=binding;
        }
    }
    private class FriendsAdapter extends RecyclerView.Adapter<FriendsViewHolder>{
        private final List<String> list;
        private FriendsAdapter(List<String> list) {this.list = list;}

        @NonNull
        @Override
        public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemFriendRequestBinding binding = ItemFriendRequestBinding.inflate(getLayoutInflater());
            return new FriendsViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull FriendsViewHolder holder, int position) {
            String text=list.get(position);
            holder.binding.friendRequestInformation.setText(text);

            holder.binding.friendRequestAccept.setOnClickListener(v -> {
                //accept
                String[] details = text.split(" \\(");
                String senderName = details[0];
                String senderStudentNum = details[1].replace(")","");

                acceptFriendRequest(senderStudentNum, position);

            });
            holder.binding.friendRequestReject.setOnClickListener(v -> {
                // reject
                String[] details = text.split(" \\(");
                String senderStudentNum = details[1].replace(")", "");
                rejectFriendRequest(senderStudentNum, position);
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }
    private void acceptFriendRequest(String senderStudentNum, int position){
            Map<String, Object> friendData = new HashMap<>();
            friendData.put("rootuser", studentNum);
            friendData.put("childuser", senderStudentNum);

            db.collection("friends").add(friendData)
                    .addOnSuccessListener(documentReference -> {
                    friendData.put("rootuser", senderStudentNum);
                    friendData.put("childuser", studentNum);

                    db.collection("friends").add(friendData)
                            .addOnSuccessListener(documentReference1 -> {
                                deleteFriendRequest(senderStudentNum, position);
                            })
                            .addOnFailureListener(e->e.printStackTrace());
    })
                    .addOnFailureListener(e->e.printStackTrace());
    }
    private void rejectFriendRequest(String senderStudentNum, int position){
            deleteFriendRequest(senderStudentNum, position);
    }
    private void deleteFriendRequest(String senderStudentNum, int position) {
        db.collection("request_friend")
                .whereEqualTo("receiverStudentNum", studentNum)
                .whereEqualTo("senderStudentNum", senderStudentNum)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        doc.getReference().delete();
                    }
                    runOnUiThread(() -> {
                        if (position>=0 && position < friendRequests.size()){
                            friendRequests.remove(position);
                            adapter.notifyItemRemoved(position);
                        }
                        loadFriendRequests(friendRequests);
                    });
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                });
        db.collection("request_friend")
                .whereEqualTo("receiverStudentNum", senderStudentNum)
                .whereEqualTo("senderStudentNum", studentNum)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        doc.getReference().delete();
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                });
    }
    private void checkEmptyView(ActivityFriendRequestDialogBinding binding, List<String> list){
        if (list.isEmpty()) {
            binding.friendRequestRecyclerView.setVisibility(View.GONE);
            binding.emptyFriendRequest.setVisibility(View.VISIBLE);
        } else{
            binding.friendRequestRecyclerView.setVisibility(View.VISIBLE);
            binding.emptyFriendRequest.setVisibility(View.GONE);
        }
    }
}