package ru.larkin.entities;

import java.util.ArrayList;
import java.util.List;

public record Wallet(double balance, String walletId, List<Operation> operations) implements Storable<String> {

    public Wallet(double balance, String walletId) {
        this(balance, walletId, new ArrayList<>());
    }

    @Override
    public String getKey() {
        return walletId;
    }
}
