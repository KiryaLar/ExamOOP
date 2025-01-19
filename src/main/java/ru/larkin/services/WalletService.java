package ru.larkin.services;

import ru.larkin.entities.Wallet;
import ru.larkin.storages.WalletStorage;

import java.util.UUID;

public class WalletService {
    private final WalletStorage wallets;

    public WalletService(WalletStorage wallets) {
        this.wallets = wallets;
    }

    public Wallet createWallet(double balance) {
        String walletId = UUID.randomUUID().toString().substring(0, 6);
        Wallet wallet = new Wallet(balance, walletId);
        wallets.save(wallet);
        return wallet;
    }
}
