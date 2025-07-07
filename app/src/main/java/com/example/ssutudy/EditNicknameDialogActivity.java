package com.example.ssutudy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ssutudy.databinding.ActivityEditNicknameDialogBinding;
import com.example.ssutudy.databinding.ActivitySettingsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class EditNicknameDialogActivity extends AppCompatActivity {
    private String studentNum;
    private FirebaseFirestore db;

    private String userNickName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityEditNicknameDialogBinding binding = ActivityEditNicknameDialogBinding.inflate(getLayoutInflater());
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(binding.getRoot());

        studentNum = getIntent().getStringExtra("studentNum");
        db=FirebaseFirestore.getInstance();

        binding.editNicknameSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userNickName = binding.editNickname.getText().toString();
                db.collection("users").document(studentNum)
                                .update("alias", userNickName).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("newNickname", userNickName);
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            }
                        });

            }
        });
        binding.editNicknameCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
}