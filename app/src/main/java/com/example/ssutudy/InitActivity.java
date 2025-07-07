package com.example.ssutudy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.ssutudy.databinding.ActivityInitBinding;
import com.example.ssutudy.databinding.ActivityTestBinding;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class InitActivity extends AppCompatActivity {

    ActivityInitBinding binding;
    FirebaseStorage storage;
    StorageReference storageRef;
    FirebaseFirestore db;

    String downloadUrl;
    String studentNum;
    String alias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInitBinding.inflate(getLayoutInflater());
        storage=FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        db = FirebaseFirestore.getInstance();
        downloadUrl = "blank";

        setContentView(binding.getRoot());

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
                                        Glide.with(InitActivity.this).load(uri2).into(binding.imageView);
                                        binding.next.setEnabled(true);
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(InitActivity.this, "사진 불러오기 실패", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        Log.d("PhotoPicker", "No media selected");
                        binding.next.setEnabled(true);
                    }
                });

        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.next.setEnabled(false);
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });

        binding.next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alias = binding.alias.getText().toString();
                studentNum = getIntent().getStringExtra("studentNum");
                //if(alias.isEmpty() || studentNum.isEmpty()) return;
                db.collection("users").document(studentNum).update(
                        "alias",alias,
                        "image_url",downloadUrl
                ).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("html","업데이트 성공");
                        String studentNum = getIntent().getStringExtra("studentNum");
                        Intent intent = new Intent(InitActivity.this, HomeActivity.class);
                        intent.putExtra("studentNum", studentNum);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                });
            }
        });
    }
}