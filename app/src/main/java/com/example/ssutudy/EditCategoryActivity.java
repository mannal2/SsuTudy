package com.example.ssutudy;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ssutudy.databinding.ActivityEditCategoryBinding;
import com.example.ssutudy.databinding.ItemGroupMemberBinding;
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

public class EditCategoryActivity extends AppCompatActivity {
    private boolean isEdit;
    private String documentId;
    private String studentNum;
    private FirebaseFirestore db;
    private List<FriendItem> friendList;
    private ArrayList<String> friendList2;
    private GroupMemberAdapter adapter;

    private EditText categoryNameInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityEditCategoryBinding binding = ActivityEditCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        studentNum = getIntent().getStringExtra("studentNum");
        db=FirebaseFirestore.getInstance();
        friendList = new ArrayList<>();
        friendList2 = new ArrayList<>();
        adapter = new GroupMemberAdapter(friendList);

        binding.groupMemberRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.groupMemberRecyclerView.setAdapter(adapter);
        categoryNameInput=binding.editCategoryName;
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.page_5);

        isEdit=getIntent().getBooleanExtra("isEdit",false);
        if (isEdit){
            documentId=getIntent().getStringExtra("documentId");
            loadCategoryDetails();
        } else {
            loadFriends();
        }


        binding.editCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        binding.editSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCategory();
            }
        });

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if(itemId == R.id.page_1){
                    Intent intent = new Intent(EditCategoryActivity.this, HomeActivity.class);
                    intent.putExtra("studentNum", studentNum);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }
                /*if(itemId == R.id.page_2){
                    Intent intent = new Intent(HomeActivity.this, CalendarActivity.class);
                    intent.putExtra("studentNum", studentNum);
                    startActivity(intent);
                    return true;
                }*/
                if(itemId == R.id.page_3){
                    db.collection("friends").whereEqualTo("rootuser", studentNum).get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.isSuccessful()){
                                        for(QueryDocumentSnapshot document : task.getResult()){
                                            //Log.d("html", document.get("childuser").toString());
                                            friendList2.add(document.get("childuser").toString());
                                        }
                                        Intent intent = new Intent(EditCategoryActivity.this, FriendsActivity.class);
                                        intent.putExtra("studentNum", studentNum);
                                        intent.putExtra("friendList", friendList2);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                        overridePendingTransition(0, 0);
                                    }
                                }
                            });
                    return true;
                }
                if(itemId == R.id.page_4){
                    Intent intent = new Intent(EditCategoryActivity.this, RecordActivity.class);
                    intent.putExtra("studentNum", studentNum);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }
                if(itemId == R.id.page_5){
                    Intent intent = new Intent(EditCategoryActivity.this, SettingsActivity.class);
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
    private void loadCategoryDetails() {
        db.collection("category").document(documentId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        categoryNameInput.setText(doc.getString("categoryName"));
                        studentNum=doc.getString("rootuser");

                        String type=doc.getString("type");
                        List<String> members = (List<String>) doc.get("member");
                        loadFriends(()->{
                            if ("group".equals(type) && members != null){
                                for (FriendItem item : friendList){
                                    if (members.contains(item.studentNum)){
                                        item.isChecked = true;
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                });
    }
    private void loadFriends() {
        loadFriends(null);
    }
    private void loadFriends(Runnable callback) {
        db.collection("friends")
                .whereEqualTo("rootuser", studentNum)
                .get()
                .addOnSuccessListener(task -> {
                    List<String> childUsers =new ArrayList<>();
                    for (DocumentSnapshot doc: task.getDocuments()){
                        String childUser =doc.getString("childuser");
                        if (childUser != null){
                            childUsers.add(childUser);
                        }
                    }
                    fetchFriendDetails(childUsers, callback);
                })
                .addOnFailureListener(e->{e.printStackTrace();});
    }
    private void fetchFriendDetails(List<String> childUsers, Runnable callback){
        for (String childUser: childUsers){
            db.collection("users").document(childUser)
                    .get()
                    .addOnSuccessListener(doc -> {
                        String name=doc.getString("name");
                        String imageUrl=doc.getString("image_url");
                        String studentNum = doc.getString("studentNum");
                        if (name!=null && studentNum!=null){
                            friendList.add(new FriendItem(name, studentNum, imageUrl));
                        }
                        adapter.notifyDataSetChanged();
                        if (callback != null) callback.run();
                    })
                    .addOnFailureListener(e->{e.printStackTrace();});
        }
    }
    private void saveCategory() {
        String categoryName = categoryNameInput.getText().toString().trim();
        if (categoryName.isEmpty()){
            return;
        }
        List<String> selectedMembers = new ArrayList<>();
        for (FriendItem item:friendList) {
            if (item.isChecked) {
                selectedMembers.add(item.studentNum);
            }
        }
        String type = selectedMembers.isEmpty() ? "custom" : "group";
        Map<String, Object> categoryData = new HashMap<>();
        categoryData.put("categoryName",categoryName);
        categoryData.put("rootuser", studentNum);
        categoryData.put("type", type);
        if ("group".equals(type)) {
            categoryData.put("member", selectedMembers);
        }
        if (isEdit){
            db.collection("category").document(documentId)
                    .set(categoryData)
                    .addOnSuccessListener(aVoid -> finish())
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                    });
        } else{
            db.collection("category")
                    .add(categoryData)
                    .addOnSuccessListener(docRef -> finish())
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                    });
        }
    }
    private static class FriendItem {
        String name;
        String studentNum;
        String imageUrl;
        boolean isChecked;

        FriendItem(String name, String studentNum, String imageUrl){
            this.name = name;
            this.studentNum = studentNum;
            this.imageUrl = imageUrl;
            this.isChecked = false;
        }
    }
    private class GroupMemberViewHolder extends RecyclerView.ViewHolder{
        private ItemGroupMemberBinding binding;
        private GroupMemberViewHolder(ItemGroupMemberBinding binding){
            super(binding.getRoot());
            this.binding=binding;
        }
    }
    private class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberViewHolder>{
        private List<FriendItem> list;
        private GroupMemberAdapter(List<FriendItem> list) {this.list = list;}

        @NonNull
        @Override
        public GroupMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemGroupMemberBinding binding = ItemGroupMemberBinding.inflate(getLayoutInflater());
            return new GroupMemberViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull GroupMemberViewHolder holder, int position) {
            FriendItem item=list.get(position);

            holder.binding.groupMemberCheckbox.setChecked(item.isChecked);
            holder.binding.groupMemberCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> item.isChecked=isChecked);
            holder.binding.groupMemberInformation.setText(String.format("%s (%s)", item.name, item.studentNum));

            if (item.imageUrl != null && !item.imageUrl.equals("blank")){
                Glide.with(EditCategoryActivity.this)
                        .load(item.imageUrl)
                        .circleCrop()
                        .into(holder.binding.groupMemberImage);
            } else{
                holder.binding.groupMemberImage.setBackgroundResource(R.drawable.circle);
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }
}