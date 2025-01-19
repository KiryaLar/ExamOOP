package ru.larkin.storages;

import ru.larkin.entities.Wallet;

public class WalletStorage extends Storage.AbstractStorage<String, Wallet> {
    public WalletStorage() {
        super("wallets.json", Wallet.class);
    }
}
