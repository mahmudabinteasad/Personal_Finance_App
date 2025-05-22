package com.example.personalfinanceapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class TransactionListActivity extends AppCompatActivity implements TransactionAdapter.TransactionClickListener {

    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private ArrayList<TransactionModel> transactionList;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private TextView emptyTextView, totalAmountTextView, headerTextView;
    private String userId;  // make accessible to whole class

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_list);

        // Initialize views
        recyclerView = findViewById(R.id.transactionRecyclerView);
        progressBar = findViewById(R.id.transactionProgressBar);
        emptyTextView = findViewById(R.id.emptyTextView);
        totalAmountTextView = findViewById(R.id.totalAmountTextView);
        headerTextView = findViewById(R.id.transactionHeader);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionList = new ArrayList<>();

        // Initialize adapter with click listener
        adapter = new TransactionAdapter(transactionList, this);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // Get userId from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", null);

        if (userId == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchTransactions(userId);
    }

    private void fetchTransactions(String userId) {
        progressBar.setVisibility(View.VISIBLE);
        emptyTextView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        CollectionReference transactionsRef = db.collection("transactions")
                .document(userId)
                .collection("userTransactions");

        transactionsRef.orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshot = task.getResult();
                        transactionList.clear();

                        double totalAmount = 0.0;

                        for (DocumentSnapshot document : snapshot) {
                            TransactionModel model = document.toObject(TransactionModel.class);
                            if (model != null) {
                                model.setId(document.getId());
                                transactionList.add(model);

                                try {
                                    double amount = Double.parseDouble(model.getAmount());
                                    if ("income".equalsIgnoreCase(model.getType())) {
                                        totalAmount += amount;
                                    } else if ("expense".equalsIgnoreCase(model.getType())) {
                                        totalAmount -= amount;
                                    }
                                } catch (NumberFormatException e) {
                                    Log.e("TransactionListActivity", "Invalid amount: " + model.getAmount());
                                }

                                Log.d("TransactionListActivity", "Transaction: " + model.getTitle() + ", Amount: " + model.getAmount());
                            }
                        }

                        adapter.notifyDataSetChanged();
                        totalAmountTextView.setText("Total: ৳" + String.format("%.2f", totalAmount));

                        if (transactionList.isEmpty()) {
                            emptyTextView.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                        }

                    } else {
                        Exception e = task.getException();
                        Log.e("TransactionListActivity", "Error loading transactions", e);
                        Toast.makeText(TransactionListActivity.this, "Error loading transactions: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("TransactionListActivity", "Failed to load transactions", e);
                    Toast.makeText(TransactionListActivity.this, "Failed to load transactions: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onEditClick(TransactionModel transaction) {
        View dialogView = getLayoutInflater().inflate(R.layout.edit_transaction_dialog, null);
        EditText editTitle = dialogView.findViewById(R.id.editTitle);
        EditText editAmount = dialogView.findViewById(R.id.editAmount);
        EditText editType = dialogView.findViewById(R.id.editType);

        editTitle.setText(transaction.getTitle());
        editAmount.setText(transaction.getAmount());
        editType.setText(transaction.getType());

        // Custom Title TextView
        TextView title = new TextView(this);
        title.setText("Edit Transaction");
        title.setPadding(40, 40, 40, 20);
        title.setTextSize(20f);
        title.setBackgroundColor(getResources().getColor(R.color.DarkestPinkAccents)); // You can define this in colors.xml
        title.setTextColor(getResources().getColor(android.R.color.white));
        title.setTypeface(null, android.graphics.Typeface.BOLD);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCustomTitle(title)
                .setView(dialogView)
                .setPositiveButton("Update", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dlg -> {
            // Update button
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.teal_700));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String newTitle = editTitle.getText().toString().trim();
                String newAmount = editAmount.getText().toString().trim();
                String newType = editType.getText().toString().trim().toLowerCase();

                if (newTitle.isEmpty() || newAmount.isEmpty() || newType.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newType.equals("income") && !newType.equals("expense")) {
                    Toast.makeText(this, "Type must be 'income' or 'expense'", Toast.LENGTH_SHORT).show();
                    return;
                }

                db.collection("transactions")
                        .document(userId)
                        .collection("userTransactions")
                        .document(transaction.getId())
                        .update("title", newTitle,
                                "amount", newAmount,
                                "type", newType)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Transaction updated", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            fetchTransactions(userId);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            });

            // Cancel button color
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.gray));
        });

        dialog.show();
    }



    @Override
    public void onDeleteClick(TransactionModel transaction) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("transactions")
                            .document(userId)
                            .collection("userTransactions")
                            .document(transaction.getId())
                            .delete()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show();
                                transactionList.remove(transaction);
                                adapter.notifyDataSetChanged();
                                fetchTransactions(userId); // Refresh list
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
