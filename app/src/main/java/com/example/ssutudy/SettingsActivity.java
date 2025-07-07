package com.example.ssutudy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.ssutudy.databinding.ActivitySettingsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
    private String studentNum;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private ArrayList<String> friendList;

    private String downloadUrl;
    private ImageView userImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySettingsBinding binding=ActivitySettingsBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.page_5);
        studentNum = getIntent().getStringExtra("studentNum");
        friendList = new ArrayList<>();

        db=FirebaseFirestore.getInstance();
        storage=FirebaseStorage.getInstance();
        storageRef=storage.getReference();
        downloadUrl="";

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if(itemId == R.id.page_1){
                    Intent intent = new Intent(SettingsActivity.this, HomeActivity.class);
                    intent.putExtra("studentNum", studentNum);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }
                if(itemId == R.id.page_2){
                    Intent intent = new Intent(SettingsActivity.this, CalendarActivity.class);
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
                                        Intent intent = new Intent(SettingsActivity.this, FriendsActivity.class);
                                        intent.putExtra("studentNum", studentNum);
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
                    Intent intent = new Intent(SettingsActivity.this, RecordActivity.class);
                    intent.putExtra("studentNum", studentNum);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }
                /*
                if(itemId == R.id.page_5){
                    Intent intent = new Intent(SettingsActivity.this, SettingsActivity.class);
                    intent.putExtra("studentNum", studentNum);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }*/
                return false;
            }
        });

        DocumentReference docRef = db.collection("users").document(studentNum);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()){
                        String userName= document.getString("alias");
                        binding.userName.setText(userName);
                    } else{
                        Log.d("SETTINGS", "No Such Document");
                    }
                } else {
                    Log.d("SETTINGS", "get failed with ", task.getException());
                }
            }
        });

        binding.userId.setText(studentNum);

        userImageView=binding.userImageView;
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()){
                        String userImage = document.getString("image_url");
                        if (Objects.equals(userImage, "blank")) return;
                        loadBackgroundImage(userImage);
                    } else{
                        Log.d("SETTINGS", "No Such Document");
                    }
                } else {
                    Log.d("SETTINGS", "get failed with ", task.getException());
                }
            }
        });


        ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    if (uri != null) {
                        StorageReference riversRef = storageRef.child("images/"+uri.getLastPathSegment());
                        UploadTask uploadTask = riversRef.putFile(uri);
                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri2) {
                                        downloadUrl = uri2.toString();
                                        Glide.with(SettingsActivity.this).load(uri2).
                                                circleCrop().into(binding.userImageView);
                                        db.collection("users").document(studentNum)
                                                .update("image_url", downloadUrl);
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SettingsActivity.this, "사진 불러오기 실패", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });



        binding.menuFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, FriendRequestDialogActivity.class)
                        .putExtra("studentNum", studentNum));
            }
        });

        binding.menuCategorySettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, CategoryActivity.class)
                        .putExtra("studentNum", studentNum));
            }
        });
        
        binding.menuEditUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());

            }
        });


        ActivityResultLauncher<Intent> editNicknameLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        String newNickname = data.getStringExtra("newNickname");
                        binding.userName.setText(newNickname);
                    }
                });

        binding.menuEditUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(SettingsActivity.this, EditNicknameDialogActivity.class)
                        .putExtra("studentNum", studentNum);
                editNicknameLauncher.launch(intent);
            }
        });

        binding.logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences tempSpf = getSharedPreferences("auto_login", Context.MODE_PRIVATE);
                tempSpf.edit().clear().apply();
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

    }
    private void loadBackgroundImage(String imageUrl){
        Glide.with(this)
                .load(imageUrl)
                .circleCrop()
                .into(userImageView);
    }
}
