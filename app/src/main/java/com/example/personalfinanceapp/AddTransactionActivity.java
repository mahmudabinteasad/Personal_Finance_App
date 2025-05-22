package com.example.personalfinanceapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;

import java.util.HashMap;
import java.util.Map;

public class AddTransactionActivity extends AppCompatActivity {

    private EditText titleEditText, amountEditText;
    private RadioGroup typeRadioGroup;
    private RadioButton incomeRadio, expenseRadio;
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
        typeRadioGroup = findViewById(R.id.typeRadioGroup);
        incomeRadio = findViewById(R.id.incomeRadio);
        expenseRadio = findViewById(R.id.expenseRadio);
        saveButton = findViewById(R.id.saveTransactionBtn);

        saveButton.setOnClickListener(v -> saveTransaction());
    }

    private void saveTransaction() {
        String title = titleEditText.getText().toString().trim();
        String amount = amountEditText.getText().toString().trim();

        if (title.isEmpty() || amount.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedId = typeRadioGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select transaction type", Toast.LENGTH_SHORT).show();
            return;
        }

        String type;
        if (selectedId == incomeRadio.getId()) {
            type = "Income";
        } else if (selectedId == expenseRadio.getId()) {
            type = "Expense";
        } else {
            type = "Unknown";
        }

        String userId = getSharedPreferences("UserSession", MODE_PRIVATE)
                .getString("userId", null);

        if (userId == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> transaction = new HashMap<>();
        transaction.put("title", title);
        transaction.put("amount", amount);
        transaction.put("type", type);
        transaction.put("timestamp", System.currentTimeMillis());

        CollectionReference transactionsRef = db.collection("transactions")
                .document(userId)
                .collection("userTransactions");

        transactionsRef.add(transaction)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Transaction Saved!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, TransactionListActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving transaction", Toast.LENGTH_SHORT).show();
                });
    }
}
