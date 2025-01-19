package ru.larkin.entities;

public record Revenue(String category, Double amount, Wallet wallet) implements Storable<String>, Operation {
    @Override
    public String getKey() {
        return category();
    }
}

