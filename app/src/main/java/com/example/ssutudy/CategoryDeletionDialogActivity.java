package com.example.ssutudy;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ssutudy.databinding.ActivityCategoryDeletionDialogBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class CategoryDeletionDialogActivity extends AppCompatActivity {
    private String documentId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        ActivityCategoryDeletionDialogBinding binding = ActivityCategoryDeletionDialogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db=FirebaseFirestore.getInstance();
        documentId=getIntent().getStringExtra("documentId");

        binding.cancelCategoryDeletionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        binding.categoryDeletionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteCategory();
            }
        });

    }

    private void deleteCategory() {
        if (documentId != null) {
            db.collection("category").document(documentId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        finish();
                    });
        }
    }
}