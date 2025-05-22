package com.example.personalfinanceapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private final List<TransactionModel> transactionList;
    private final TransactionClickListener listener;

    // Constructor with click listener
    public TransactionAdapter(List<TransactionModel> transactionList, TransactionClickListener listener) {
        this.transactionList = transactionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransactionModel model = transactionList.get(position);

        if (model != null) {
            holder.title.setText(model.getTitle());

            try {
                double amount = Double.parseDouble(model.getAmount());
                holder.amount.setText("৳ " + String.format("%.2f", amount));

                if ("income".equalsIgnoreCase(model.getType())) {
                    holder.amount.setTextColor(Color.parseColor("#2E7D32")); // Green
                    holder.type.setText("Income");
                } else if ("expense".equalsIgnoreCase(model.getType())) {
                    holder.amount.setTextColor(Color.parseColor("#C62828")); // Red
                    holder.type.setText("Expense");
                } else {
                    holder.amount.setTextColor(Color.BLACK);
                    holder.type.setText(model.getType());
                }
            } catch (NumberFormatException e) {
                holder.amount.setText("৳ 0.00");
                holder.amount.setTextColor(Color.GRAY);
                holder.type.setText(model.getType());
            }

            // Set click listeners for Edit and Delete buttons
            holder.btnEdit.setOnClickListener(v -> listener.onEditClick(model));
            holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(model));
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, amount, type;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.transactionTitle);
            amount = itemView.findViewById(R.id.transactionAmount);
            type = itemView.findViewById(R.id.transactionType);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    // Interface for edit and delete click actions
    public interface TransactionClickListener {
        void onEditClick(TransactionModel transaction);
        void onDeleteClick(TransactionModel transaction);
    }
}
