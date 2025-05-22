package com.example.personalfinanceapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.Map;

public class AddTransactionActivity extends AppCompatActivity {

    private EditText titleEditText, amountEditText;
    private Button saveButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Find views by ID
        titleEditText = findViewById(R.id.titleEditText);
        amountEditText = findViewById(R.id.amountEditText);
        saveButton = findViewById(R.id.saveTransactionBtn);

        saveButton.setOnClickListener(v -> saveTransaction());
    }

    private void saveTransaction() {
        // Get values from input fields
        String title = titleEditText.getText().toString();
        String amount = amountEditText.getText().toString();

        if (title.isEmpty() || amount.isEmpty()) {
            Toast.makeText(AddTransactionActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = getSharedPreferences("UserSession", MODE_PRIVATE).getString("userId", null);

        if (userId == null) {
            Toast.makeText(AddTransactionActivity.this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new transaction object
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("title", title);
        transaction.put("amount", amount);
        transaction.put("type", "Expense");  // Set this dynamically if needed
        transaction.put("timestamp", System.currentTimeMillis());  // Add timestamp for ordering

        // Add transaction to Firestore under userId
        CollectionReference transactionsRef = db.collection("transactions").document(userId).collection("userTransactions");
        transactionsRef.add(transaction)
                .addOnSuccessListener(documentReference -> {
                    // Success: Show success message and navigate to TransactionListActivity
                    Toast.makeText(AddTransactionActivity.this, "Transaction Saved!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AddTransactionActivity.this, TransactionListActivity.class);
                    startActivity(intent);  // Navigate to View Transactions
                    finish();  // Close AddTransactionActivity
                })
                .addOnFailureListener(e -> {
                    // Failure: Show error message
                    Toast.makeText(AddTransactionActivity.this, "Error saving transaction", Toast.LENGTH_SHORT).show();
                });
    }
}
