package com.example.personalfinanceapp;

public class TransactionModel {
    private String id;
    private String title;
    private String amount;
    private String type;

    public TransactionModel() {
        // Required for Firestore (empty constructor)
    }

    public TransactionModel(String id, String title, String amount, String type) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setType(String type) {
        this.type = type;
    }
}