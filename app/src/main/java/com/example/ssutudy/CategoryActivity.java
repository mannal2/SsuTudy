package com.example.ssutudy;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ssutudy.databinding.ActivityCategoryBinding;
import com.example.ssutudy.databinding.ItemSettingsCategoryBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {
    private String studentNum;
    private FirebaseFirestore db;
    private CategoryAdapter adapter;
    private List<CategoryItem> categoryList;
    private ArrayList<String> friendList;

    private final ActivityResultLauncher<Intent> updateCategoryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK){
                    loadCategories();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCategoryBinding binding = ActivityCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        studentNum = getIntent().getStringExtra("studentNum");
        db = FirebaseFirestore.getInstance();
        categoryList = new ArrayList<>();
        adapter = new CategoryAdapter(categoryList);
        friendList = new ArrayList<>();

        binding.categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.categoryRecyclerView.setAdapter(adapter);

        loadCategories();

        binding.addCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CategoryActivity.this, EditCategoryActivity.class)
                        .putExtra("studentNum", studentNum)
                        .putExtra("isEdit", false);
                updateCategoryLauncher.launch(intent);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.page_5);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if(itemId == R.id.page_1){
                    Intent intent = new Intent(CategoryActivity.this, HomeActivity.class);
                    intent.putExtra("studentNum", studentNum);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }
                if(itemId == R.id.page_2){
                    Intent intent = new Intent(CategoryActivity.this, CalendarActivity.class);
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
                                        Intent intent = new Intent(CategoryActivity.this, FriendsActivity.class);
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
                    Intent intent = new Intent(CategoryActivity.this, RecordActivity.class);
                    intent.putExtra("studentNum", studentNum);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }

                if(itemId == R.id.page_5){
                    Intent intent = new Intent(CategoryActivity.this, SettingsActivity.class);
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
    protected  void onResume(){
        super.onResume();
        loadCategories();
    }
    private void loadCategories() {
        db.collection("category")
                .whereEqualTo("rootuser", studentNum)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        categoryList.clear();
                        for (DocumentSnapshot document : task. getResult()) {
                            String categoryName = document.getString("categoryName");
                            String type = document.getString("type");
                            String documentId = document.getId();
                            if (categoryName != null && type!= null){
                                categoryList.add(new CategoryItem(categoryName, type, documentId));
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> e.printStackTrace());
    }
    private static class CategoryItem {
        String categoryName;
        String type;
        String documentId;

        CategoryItem(String categoryName, String type, String documentId){
            this.categoryName=categoryName;
            this.type=type;
            this.documentId = documentId;
        }
    }
    private class CategoryViewHolder extends RecyclerView.ViewHolder{
        private ItemSettingsCategoryBinding binding;
        private CategoryViewHolder(ItemSettingsCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
    private class CategoryAdapter extends RecyclerView.Adapter<CategoryViewHolder>{
        private List<CategoryItem> list;
        private CategoryAdapter(List<CategoryItem> list) {this.list = list;}

        @NonNull
        @Override
        public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemSettingsCategoryBinding binding = ItemSettingsCategoryBinding.inflate(getLayoutInflater());
            return new CategoryViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
            CategoryItem item = list.get(position);
            holder.binding.itemNameCategory.setText(item.categoryName);

            if ("group".equals(item.type)){
                holder.binding.itemIconCategory.setBackgroundResource(R.drawable.group_icon);
            } else{
                holder.binding.itemIconCategory.setBackgroundResource(R.drawable.private_icon);
            }

            holder.binding.itemEditCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(CategoryActivity.this, EditCategoryActivity.class)
                            .putExtra("documentId", item.documentId)
                            .putExtra("isEdit", true);
                    updateCategoryLauncher.launch(intent);
                }
            });
            holder.binding.itemDeleteCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(CategoryActivity.this, CategoryDeletionDialogActivity.class);
                    intent.putExtra("documentId", item.documentId);
                    updateCategoryLauncher.launch(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }
}