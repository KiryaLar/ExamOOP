package ru.larkin.entities;

public record Expense(String category, Double amount, Double limit, Wallet wallet) implements Storable<String>, Operation {


    @Override
    public String getKey() {
        return category();
    }
}
