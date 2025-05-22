package com.example.personalfinanceapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class TransactionListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private ArrayList<TransactionModel> transactionList;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_list);

        recyclerView = findViewById(R.id.transactionRecyclerView);
        progressBar = findViewById(R.id.transactionProgressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionList = new ArrayList<>();
        adapter = new TransactionAdapter(transactionList);
        recyclerView.setAdapter(adapter);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);

        if (userId == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchTransactions(userId);
    }

    private void fetchTransactions(String userId) {
        progressBar.setVisibility(View.VISIBLE);

        // Firestore collection reference for transactions
        CollectionReference transactionsRef = db.collection("transactions").document(userId).collection("userTransactions");

        transactionsRef.orderBy("timestamp", Query.Direction.DESCENDING)  // Optionally order by timestamp
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshot = task.getResult();
                        transactionList.clear();
                        for (DocumentSnapshot document : snapshot) {
                            TransactionModel model = document.toObject(TransactionModel.class);
                            if (model != null) {
                                model.setId(document.getId());
                                transactionList.add(model);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(TransactionListActivity.this, "Error loading transactions.", Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TransactionListActivity.this, "Failed to load transactions.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
    }
}
